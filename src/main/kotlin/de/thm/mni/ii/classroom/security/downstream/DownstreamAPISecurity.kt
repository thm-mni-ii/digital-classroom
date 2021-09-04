package de.thm.mni.ii.classroom.security.downstream

import de.thm.mni.ii.classroom.properties.ClassroomProperties
import de.thm.mni.ii.classroom.security.exception.UnauthorizedException
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 * Security component authorizing requests via the downstream BBB-like API.
 * Basic mechanism: The downstream gateway calculates a checksum of the request URL and a shared secret.
 * The checksum is validated by recalculating it.
 * (mockup) checksum calculation: SHA1("lastPathSegment" + "queryStringWithoutChecksum" + "sharedSecret")
 *
 * @param classroomProperties The property object containing the shared secret.
 */
@Component
class DownstreamAPISecurity(private val classroomProperties: ClassroomProperties) {

    private val logger = LoggerFactory.getLogger(DownstreamAPISecurity::class.java)

    /**
     * Function constructing the main AuthenticationWebFilter.
     * Accumulates the ReactiveAuthenticationManager and ServerAuthenticationConverter and
     * constrains the filter to requests at a specific path (/api/\*).
     */
    fun downstreamAPIFilter(): AuthenticationWebFilter {
        val authManager = reactiveAuthenticationManager()
        val downstreamAPIFilter = AuthenticationWebFilter(authManager)
        downstreamAPIFilter.setRequiresAuthenticationMatcher(
            ServerWebExchangeMatchers.pathMatchers("/api/*")
        )
        downstreamAPIFilter.setServerAuthenticationConverter(downstreamAPIAuthenticationConverter())
        return downstreamAPIFilter
    }

    /**
     * The ReactiveAuthenticationManager validating the Authentication object.
     * The Authentication object is constructed from the ServerExchange within the downstreamAPIAuthenticationConverter.
     */
    private fun reactiveAuthenticationManager() = ReactiveAuthenticationManager { authentication ->
        Mono.create {
            authentication as DownstreamAPIAuthentication
            if (authentication.isAuthenticated) {
                it.success(authentication)
            } else {
                logger.info(
                    "Incorrect checksum from {}! Given: {}, Calculated: {}",
                    authentication.name,
                    authentication.credentials,
                    authentication.details
                )
                it.error(UnauthorizedException("Incorrect checksum!"))
            }
        }
    }

    /**
     * Constructs a ServerAuthenticationConverter converting the ServerWebExchange to an DownstreamAPIAuthentication object.
     * @see DownstreamAPIAuthentication
     * @return the ServerAuthenticationConverter
     */
    private fun downstreamAPIAuthenticationConverter(): ServerAuthenticationConverter {
        /**
         * Creates the AuthenticationObject with the given Checksum from the HTTP Request
         *  and the host name of the requesting service.
         * @param exchange the ServerWebExchange object
         * @return A Mono containing a Pair of the DownstreamAPIAuthentication and
         *  the ServerWebExchange for further calculations.
         */
        fun createAuthentication(exchange: ServerWebExchange): Mono<Pair<DownstreamAPIAuthentication, ServerWebExchange>> {
            return Mono.create {
                val host = exchange.request.headers.host?.hostString ?: exchange.request.remoteAddress?.hostString ?: "UNKNOWN HOST"
                val givenChecksum = exchange.request.queryParams.getFirst("checksum")
                if (givenChecksum == null) {
                    it.error(UnauthorizedException())
                } else {
                    it.success(Pair(DownstreamAPIAuthentication(host, givenChecksum), exchange))
                }
            }
        }

        /**
         * Recalculates the checksum with the information of the ServerWebExchange.
         * @param input Pair of the DownstreamAPIAuthentication and ServerWebExchange as created by createAuthentication
         * @return Mono containing the complete DownstreamAPIAuthentication
         * @see DownstreamAPIAuthentication
         */
        fun calculateChecksum(input: Pair<DownstreamAPIAuthentication, ServerWebExchange>): Mono<DownstreamAPIAuthentication> {
            return Mono.create {
                val authentication = input.first
                val exchange = input.second
                val query = exchange.request.uri.rawQuery?.replace(Regex("&?checksum=\\w+"), "") ?: ""
                val apiCall = exchange.request.path.value().substringAfterLast("/")
                val calculatedChecksum = DigestUtils.sha1Hex("$apiCall$query${classroomProperties.sharedSecret}")
                authentication.calculatedChecksum = calculatedChecksum
                it.success(authentication)
            }
        }

        return ServerAuthenticationConverter { exchange ->
            exchange.toMono()
                .flatMap(::createAuthentication)
                .flatMap(::calculateChecksum)
        }
    }

}
