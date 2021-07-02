package de.thm.mni.ii.classroom.security.classroom

import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 * Security component authenticating and authorizing requests at the classroom API using a single-use session token.
 * Basic mechanism: Users are pre-authenticating via /api/join by the GATEWAY service and placed inside the
 * ClassroomUserDetailsRepository with an associated session token.
 * This session token is sent by the client using the classroom and exchanged for a
 * Json Web Token (JWT) in the ServerAuthenticatedSuccessHandler.
 *
 * @param userDetailsRepository A DAO offering access to valid session token and UserDetails.
 * @param jwtService A service creating and verifying JWT with UserDetails.
 * @see ClassroomUserDetailsRepository
 * @see ClassroomJWTService
 * @see de.thm.mni.ii.classroom.security.downstream.DownstreamAPISecurity
 */
@Component
class SessionTokenSecurity(private val userDetailsRepository: ClassroomUserDetailsRepository,
                           private val jwtService: ClassroomJWTService) {
    /**
     * Function constructing the main {@link AuthenticationWebFilter}
     * Accumulates the ReactiveAuthenticationManager, ServerAuthenticationConverter, and ServerAuthenticationSuccessHandler
     * constrains the filter to requests at a specific path (/classroom/\**).
     * @see AuthenticationWebFilter
     * @see ReactiveAuthenticationManager
     * @see ServerAuthenticationConverter
     * @see ServerAuthenticationSuccessHandler
     */
    fun sessionTokenFilter(): AuthenticationWebFilter {
        val sessionTokenFilter: AuthenticationWebFilter
        val authManager = this.reactiveAuthenticationManager()
        val successHandler: ServerAuthenticationSuccessHandler = this.sessionTokenAuthenticationSuccessHandler()
        sessionTokenFilter = AuthenticationWebFilter(authManager)
        sessionTokenFilter.setRequiresAuthenticationMatcher(
            AndServerWebExchangeMatcher(
                ServerWebExchangeMatchers.pathMatchers("/join"),
                ServerWebExchangeMatcher {
                    if (it.request.queryParams.containsKey("sessionToken")) {
                        ServerWebExchangeMatcher.MatchResult.match()
                    } else {
                        ServerWebExchangeMatcher.MatchResult.notMatch()
                    }
                }
        ))
        sessionTokenFilter.setServerAuthenticationConverter(this.sessionTokenAuthenticationConverter())
        sessionTokenFilter.setAuthenticationSuccessHandler(successHandler)
        return sessionTokenFilter
    }

    /**
     * The ReactiveAuthenticationManager validating the ClassroomAuthentication object.
     * If the ClassroomAuthentication object is not valid, the JWTSecurity config will try to authenticate the request.
     * The Authentication object is constructed from the ServerExchange within the sessionTokenAuthenticationConverter.
     * @see SessionTokenSecurity.sessionTokenAuthenticationConverter
     * @see ReactiveAuthenticationManager
     * @see JWTSecurity
     * @see ClassroomAuthentication
     */
    private fun reactiveAuthenticationManager() = ReactiveAuthenticationManager { auth ->
        val user = userDetailsRepository.findBySessionToken(auth.credentials as String)
        val jwt = user?.let { jwtService.createToken(user) } ?: ""
        Mono.just(ClassroomAuthentication(user, jwt, auth.credentials as String))
    }

    /**
     *
     */
    private fun sessionTokenAuthenticationConverter() = ServerAuthenticationConverter { exchange ->
        fun getSessionToken(exchange: ServerWebExchange): Mono<Authentication> {
            return Mono.create {
                it.success(
                    UsernamePasswordAuthenticationToken(null, exchange.request.queryParams.getFirst("sessionToken"), null)
                )
            }
        }
        exchange.toMono().flatMap(::getSessionToken)
    }

    private fun sessionTokenAuthenticationSuccessHandler(): ServerAuthenticationSuccessHandler {
        fun getHttpAuthHeaderValue(authentication: ClassroomAuthentication): String {
            return "Bearer ${authentication.credentials}"
        }

        return ServerAuthenticationSuccessHandler {
            webFilterExchange, authentication ->
                val exchange = webFilterExchange.exchange
                exchange.response
                    .headers
                    .add(HttpHeaders.AUTHORIZATION, getHttpAuthHeaderValue(authentication as ClassroomAuthentication))
                webFilterExchange.chain.filter(exchange)
        }
    }
}

