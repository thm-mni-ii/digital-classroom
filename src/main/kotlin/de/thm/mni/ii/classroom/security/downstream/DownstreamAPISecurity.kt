package de.thm.mni.ii.classroom.security.downstream

import de.thm.mni.ii.classroom.properties.DownstreamGatewayProperties
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

@Component
class DownstreamAPISecurity(private val downstreamGatewayProperties: DownstreamGatewayProperties) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun downstreamAPIFilter(): AuthenticationWebFilter {
        val authManager = ReactiveAuthenticationManager { authentication ->
            Mono.create {
                authentication as DownstreamAPIAuthentication
                if (authentication.isAuthenticated) {
                    it.success(authentication)
                } else {
                    logger.info("Incorrect checksum from {}! Given: {}, Calculated: {}",
                        authentication.name,
                        authentication.credentials,
                        authentication.details)
                    it.error(UnauthorizedException("Incorrect checksum!"))
                }
            }
        }
        val downstreamAPIFilter = AuthenticationWebFilter(authManager)
        downstreamAPIFilter.setRequiresAuthenticationMatcher(
            ServerWebExchangeMatchers.pathMatchers("/api/*")
        )
        downstreamAPIFilter.setServerAuthenticationConverter(downstreamAPIAuthenticationConverter())
        return downstreamAPIFilter
    }

    private fun downstreamAPIAuthenticationConverter(): ServerAuthenticationConverter {
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

        fun calculateChecksum(input: Pair<DownstreamAPIAuthentication, ServerWebExchange>): Mono<DownstreamAPIAuthentication> {
            return Mono.create {
                val authentication = input.first
                val exchange = input.second
                val query = exchange.request.uri.rawQuery?.replace(Regex("&checksum=\\w+"), "") ?: ""
                val apiCall = exchange.request.uri.toString().substringAfterLast("/").substringBefore("?")
                val calculatedChecksum = DigestUtils.sha1Hex("$apiCall$query${downstreamGatewayProperties.sharedSecret}")
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