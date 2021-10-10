package de.thm.mni.ii.classroom.security.classroom

import de.thm.mni.ii.classroom.model.classroom.UserCredentials
import de.thm.mni.ii.classroom.security.jwt.ClassroomAuthentication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler
import org.springframework.security.config.annotation.rsocket.RSocketSecurity
import org.springframework.security.messaging.handler.invocation.reactive.AuthenticationPrincipalArgumentResolver
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor
import reactor.core.publisher.Mono

@Configuration
class ClassroomRSocketJwtSecurity {

    @Bean
    fun rSocketInterceptor(
        rSocket: RSocketSecurity,
        jwtReactiveAuthenticationManager: JwtReactiveAuthenticationManager
    ): PayloadSocketAcceptorInterceptor {
        rSocket.authorizePayload {
            it.route("stream/users").authenticated()
                .anyRequest().authenticated()
                .anyExchange().permitAll()
        }.jwt {
            it.authenticationManager(jwtReactiveAuthenticationManager)
        }
        return rSocket.build()
    }

    @Bean
    fun jwtReactiveAuthenticationManager(
        decoder: ReactiveJwtDecoder,
        converter: Converter<Jwt, Mono<ClassroomAuthentication>>
    ): JwtReactiveAuthenticationManager {
        val manager = JwtReactiveAuthenticationManager(decoder)
        manager.setJwtAuthenticationConverter(converter)
        return manager
    }

    @Bean
    fun messageHandler(strategies: RSocketStrategies): RSocketMessageHandler {
        val mh = RSocketMessageHandler()
        mh.argumentResolverConfigurer.addCustomResolver(
            AuthenticationPrincipalArgumentResolver()
        )
        mh.rSocketStrategies = strategies
        return mh
    }

    @Bean
    fun jwtToAuthenticationConverter(): Converter<Jwt, Mono<ClassroomAuthentication>> {
        return Converter<Jwt, Mono<ClassroomAuthentication>> { jwt ->

            fun delegateMono(jwt: Jwt): ClassroomAuthentication {
                val userCredentials = UserCredentials(jwt.claims)
                return ClassroomAuthentication(userCredentials, jwt.tokenValue)
            }

            Mono.just(jwt).map(::delegateMono)
        }
    }
}
