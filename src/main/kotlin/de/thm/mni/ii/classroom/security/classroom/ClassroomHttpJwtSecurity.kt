package de.thm.mni.ii.classroom.security.classroom

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.function.Predicate

@Configuration
class ClassroomHttpJwtSecurity(
    private val jwtReactiveAuthenticationManager: JwtReactiveAuthenticationManager
) {

    @Bean
    fun jwtFilter(serverAuthenticationConverter: ServerAuthenticationConverter): AuthenticationWebFilter {
        val authManager = jwtReactiveAuthenticationManager
        val jwtFilter = AuthenticationWebFilter(authManager)
        jwtFilter.setRequiresAuthenticationMatcher(
            AndServerWebExchangeMatcher(
                ServerWebExchangeMatchers.pathMatchers(
                    "/classroom-api", "/classroom-api/**", "/websocket/**", "/websocket"
                ),
                ServerWebExchangeMatcher {
                    if (it.request.path.value().endsWith("/classroom-api/join"))
                        ServerWebExchangeMatcher.MatchResult.notMatch()
                    else ServerWebExchangeMatcher.MatchResult.match()
                }
            )
        )

        jwtFilter.setServerAuthenticationConverter(serverAuthenticationConverter)
        return jwtFilter
    }

    @Bean
    fun classroomHttpJwtAuthenticationConverter(): ServerAuthenticationConverter {
        return ServerAuthenticationConverter { exchange ->
            val bearer = "Bearer "
            val matchBearerLength = Predicate { authValue: String -> authValue.length > bearer.length }
            fun isolateBearerValue(authValue: String) = Mono.just(
                authValue.substring(bearer.length)
            )

            fun extract(serverWebExchange: ServerWebExchange): Mono<String> {
                return Mono.justOrEmpty(
                    serverWebExchange.request
                        .headers
                        .getFirst(HttpHeaders.AUTHORIZATION)
                )
            }

            fun createAuthenticationObject(jwt: String): Mono<BearerTokenAuthenticationToken> {
                return Mono.create {
                    it.success(BearerTokenAuthenticationToken(jwt))
                }
            }

            exchange.toMono()
                .flatMap(::extract)
                .filter(matchBearerLength)
                .flatMap(::isolateBearerValue)
                .flatMap(::createAuthenticationObject)
        }
    }
}
