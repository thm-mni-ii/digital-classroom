package de.thm.mni.ii.classroom.security

import de.thm.mni.ii.classroom.security.classroom.JWTSecurity
import de.thm.mni.ii.classroom.security.classroom.SessionTokenSecurity
import de.thm.mni.ii.classroom.security.downstream.DownstreamAPISecurity
import org.springframework.context.annotation.Bean
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferFactory
import org.springframework.core.io.buffer.DefaultDataBuffer
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.HttpHeaders.WWW_AUTHENTICATE
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.nio.ByteBuffer
import kotlin.text.Charsets.UTF_8

/**
 * Main security configuration of the digital classroom.
 */
@EnableWebFluxSecurity
class SecurityConfiguration(private val sessionTokenSecurity: SessionTokenSecurity,
                            private val downstreamAPISecurity: DownstreamAPISecurity,
                            private val jwtSecurity: JWTSecurity) {

    /**
     * Security filter chain for digital classroom.
     * Checks if a request is authenticated by sessionToken, or JWT and authorization via the resolved user role
     * or the valid checksum in downstream api.
     * @see DownstreamAPISecurity
     * @see SessionTokenSecurity
     * @see JWTSecurity
     */
    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http.exceptionHandling()
            .authenticationEntryPoint { exchange, denied ->
                val response = exchange.response
                response.statusCode = HttpStatus.UNAUTHORIZED
                val buffer = response.bufferFactory().wrap(denied.message?.toByteArray(UTF_8) ?: ByteArray(0))
                response.writeWith(Mono.just(buffer))
                exchange.mutate().response(response)
                Mono.empty()
            }
        return http
            .httpBasic().disable()
            .csrf().disable()
            .cors().disable()
            .logout().disable()
            // Filter for Downstream API. This is active at the routes /api/* and resolves authorized requests to the role GATEWAY.
            .addFilterAt(downstreamAPISecurity.downstreamAPIFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
            // Filters for classroom access. Active at /classroom and /classroom/**.
            // Resolves authenticated requests to a User with a role STUDENT, TUTOR or TEACHER.
            .addFilterAt(sessionTokenSecurity.sessionTokenFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
            // As above, but with JWT
            .addFilterAt(jwtSecurity.jwtFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
            .authorizeExchange() // Exchanges at the path below with Role GATEWAY is authorized.
            .pathMatchers("/api/*")
            .hasAuthority("GATEWAY")
            .and()
            .authorizeExchange() // Exchanges at the paths below with UserRoles STUDENT, TUTOR or TEACHER are authorized.
            .pathMatchers("/classroom-api/**")
            .hasAnyAuthority("STUDENT", "TUTOR", "TEACHER")
            .and()
            .authorizeExchange()
            .pathMatchers("/*", "/static/**", "/classroom/*", "/classroom", "/rsocket/**", "/rsocket")
            .permitAll()
            .and()
            .build()
    }

}


