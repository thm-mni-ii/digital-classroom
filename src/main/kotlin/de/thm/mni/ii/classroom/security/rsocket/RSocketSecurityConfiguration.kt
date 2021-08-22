package de.thm.mni.ii.classroom.security.rsocket

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler
import org.springframework.security.config.annotation.rsocket.RSocketSecurity
import org.springframework.security.messaging.handler.invocation.reactive.AuthenticationPrincipalArgumentResolver
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.*
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor

@Configuration
class RSocketSecurityConfiguration {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun rSocketInterceptor(rSocket: RSocketSecurity,
                           decoder: ReactiveJwtDecoder,
                           converter: JwtClassroomAuthenticationConverterAdapter): PayloadSocketAcceptorInterceptor {
        rSocket.authorizePayload {
            it.route("stream/users").authenticated()
                .anyRequest().authenticated()
                .anyExchange().permitAll()
        }.jwt {
            it.authenticationManager(this.jwtReactiveAuthenticationManager(decoder, converter))
        }
        return rSocket.build()
    }

    fun jwtReactiveAuthenticationManager(
        decoder: ReactiveJwtDecoder,
        converter: JwtClassroomAuthenticationConverterAdapter
    ): JwtReactiveAuthenticationManager {
        val manager = JwtReactiveAuthenticationManager(decoder)
        manager.setJwtAuthenticationConverter(converter)
        return manager
    }

    @Bean
    fun messageHandler(strategies: RSocketStrategies): RSocketMessageHandler {
        return getMessageHandler(strategies)
    }

    private fun getMessageHandler(strategies: RSocketStrategies?): RSocketMessageHandler {
        val mh = RSocketMessageHandler()
        mh.argumentResolverConfigurer.addCustomResolver(
            AuthenticationPrincipalArgumentResolver()
        )
        mh.rSocketStrategies = strategies!!
        return mh
    }

}
