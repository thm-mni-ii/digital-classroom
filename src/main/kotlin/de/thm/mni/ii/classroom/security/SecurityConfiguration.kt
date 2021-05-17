package de.thm.mni.ii.classroom.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
class SecurityConfiguration(private val authenticationManager: AuthenticationManager) {

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        // Disable default security.
        return http.httpBasic().disable()
            .formLogin().disable()
            .csrf().disable()
            .logout().disable()
            .authenticationManager(authenticationManager)
            .authorizeExchange().anyExchange().authenticated().and().build()
    }
}


