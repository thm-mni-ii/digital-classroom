package de.thm.mni.ii.classroom.security

import de.thm.mni.ii.classroom.security.classroom.JWTSecurity
import de.thm.mni.ii.classroom.security.classroom.SessionTokenSecurity
import de.thm.mni.ii.classroom.security.downstream.DownstreamAPISecurity
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@EnableWebFluxSecurity
class SecurityConfiguration(private val sessionTokenSecurity: SessionTokenSecurity,
                            private val downstreamAPISecurity: DownstreamAPISecurity,
                            private val jwtSecurity: JWTSecurity) {

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http.httpBasic().disable()
            .csrf().disable()
            .addFilterAt(downstreamAPISecurity.downstreamAPIFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
            .addFilterAt(sessionTokenSecurity.sessionTokenFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
            .addFilterAt(jwtSecurity.jwtFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
            .authorizeExchange()
            .pathMatchers("/api/*")
            .hasAuthority("GATEWAY")
            .and()
            .authorizeExchange()
            .pathMatchers("/classroom", "/classroom/**")
            .hasAnyAuthority("STUDENT", "TUTOR", "TEACHER")
            .and()
            .build()
    }

}


