package de.thm.mni.ii.classroom.security.rsocket

import de.thm.mni.ii.classroom.security.classroom.ClassroomAuthentication
import de.thm.mni.ii.classroom.security.classroom.JWTSecurity
import io.netty.buffer.ByteBuf
import io.rsocket.metadata.CompositeMetadata
import io.rsocket.metadata.WellKnownMimeType
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.rsocket.RSocketSecurity
import org.springframework.security.config.annotation.rsocket.RSocketSecurity.AuthorizePayloadsSpec
import org.springframework.security.core.Authentication
import org.springframework.security.rsocket.api.PayloadInterceptor
import org.springframework.security.rsocket.authentication.AuthenticationPayloadInterceptor
import org.springframework.security.rsocket.authentication.PayloadExchangeAuthenticationConverter
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets

@Configuration
class RSocketSecurityConfiguration(private val jwtSecurity: JWTSecurity) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun rSocketInterceptor(rSocket: RSocketSecurity): PayloadSocketAcceptorInterceptor {
        rSocket.addPayloadInterceptor(payloadInterceptor()).authorizePayload {
            it.anyExchange().authenticated()
        }
        return rSocket.build()
    }

    @Bean
    fun payloadInterceptor(): PayloadInterceptor {
        val authMan = AuthenticationPayloadInterceptor(jwtSecurity.jwtAuthenticationManager())
        authMan.setAuthenticationConverter(payloadExchangeAuthenticationConverter())
        return authMan
    }

    fun payloadExchangeAuthenticationConverter() = PayloadExchangeAuthenticationConverter { exchange ->
        val mimeType = WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.string
        val metadata: ByteBuf = exchange.payload.metadata()
        val compositeMetadata = CompositeMetadata(metadata, true)
        for (entry in compositeMetadata) {
            if (mimeType == entry.mimeType) {
                val content = entry.content
                val token = content.toString(StandardCharsets.US_ASCII).substring(1)
                return@PayloadExchangeAuthenticationConverter Mono.just<Authentication>(UsernamePasswordAuthenticationToken(null, token, null))
            }
        }
        return@PayloadExchangeAuthenticationConverter Mono.empty()
    }

}
