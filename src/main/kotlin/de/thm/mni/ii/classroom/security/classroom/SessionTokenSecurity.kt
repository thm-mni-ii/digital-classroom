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

@Component
class SessionTokenSecurity(private val userDetailsRepository: ClassroomUserDetailsRepository,
                           private val jwtService: ClassroomJWTService
) {

    fun sessionTokenFilter(): AuthenticationWebFilter {
        val sessionTokenFilter: AuthenticationWebFilter
        val authManager = ReactiveAuthenticationManager { auth ->
            val user = userDetailsRepository.findBySessionToken(auth.credentials as String)
            val jwt = user?.let { jwtService.createToken(user) } ?: ""
            Mono.just(ClassroomAuthentication(user, jwt, auth.credentials as String))
        }
        val successHandler: ServerAuthenticationSuccessHandler = sessionTokenAuthenticationSuccessHandler()
        sessionTokenFilter = AuthenticationWebFilter(authManager)
        sessionTokenFilter.setRequiresAuthenticationMatcher(AndServerWebExchangeMatcher(
            ServerWebExchangeMatchers.pathMatchers("/classroom", "/classroom/**"),
            ServerWebExchangeMatcher {
                if (it.request.queryParams.containsKey("sessionToken")) {
                    ServerWebExchangeMatcher.MatchResult.match()
                } else {
                    ServerWebExchangeMatcher.MatchResult.notMatch()
                }
            }
        ))
        sessionTokenFilter.setServerAuthenticationConverter(SessionTokenAuthenticationConverter())
        sessionTokenFilter.setAuthenticationSuccessHandler(successHandler)
        return sessionTokenFilter
    }

    class SessionTokenAuthenticationConverter: ServerAuthenticationConverter {
        private fun getSessionToken(exchange: ServerWebExchange): Mono<Authentication> {
            return Mono.create {
                it.success(
                    UsernamePasswordAuthenticationToken(null, exchange.request.queryParams.getFirst("sessionToken"), null)
                )
            }
        }

        override fun convert(exchange: ServerWebExchange): Mono<Authentication> {
            return exchange.toMono().flatMap(this::getSessionToken)
        }
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

