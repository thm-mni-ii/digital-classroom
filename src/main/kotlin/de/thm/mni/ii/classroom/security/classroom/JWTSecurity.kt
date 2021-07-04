package de.thm.mni.ii.classroom.security.classroom

import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.function.Predicate

@Component
class JWTSecurity(private val userDetailsRepository: ClassroomUserDetailsRepository,
                  private val jwtService: ClassroomJWTService) {

    fun jwtFilter(): AuthenticationWebFilter {
        val authManager = ReactiveAuthenticationManager { auth ->
            Mono.create {
                val jwt = auth.credentials as String
                val user = jwtService.authorize(jwt)
                if (user != null) {
                    it.success(
                        ClassroomAuthentication(user, jwt, "")
                    )
                } else {
                    it.success()
                }
            }
        }
        val jwtFilter = AuthenticationWebFilter(authManager)
        jwtFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/classroom-api", "/classroom-api/**", "/websocket/**", "/websocket"))
        jwtFilter.setServerAuthenticationConverter(JWTAuthenticationConverter())
        return jwtFilter
    }

    class JWTAuthenticationConverter: ServerAuthenticationConverter {
        private val BEARER = "Bearer "
        private val matchBearerLength = Predicate { authValue: String -> authValue.length > BEARER.length }
        private fun isolateBearerValue(authValue: String) = Mono.just(
            authValue.substring(BEARER.length)
        )

        private fun extract(serverWebExchange: ServerWebExchange): Mono<String> {
            return Mono.justOrEmpty(
                serverWebExchange.request
                    .headers
                    .getFirst(HttpHeaders.AUTHORIZATION)
            )
        }

        private fun createAuthenticationObject(jwt: String): Mono<UsernamePasswordAuthenticationToken> {
            return Mono.create {
                it.success(
                    UsernamePasswordAuthenticationToken(null, jwt, null)
                )
            }
        }

        override fun convert(exchange: ServerWebExchange): Mono<Authentication> {
            return exchange.toMono()
                .flatMap(this::extract)
                .filter(matchBearerLength)
                .flatMap(this::isolateBearerValue)
                .flatMap(this::createAuthenticationObject)
        }
    }
}

