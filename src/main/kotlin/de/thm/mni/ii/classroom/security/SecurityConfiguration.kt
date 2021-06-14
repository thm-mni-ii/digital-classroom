package de.thm.mni.ii.classroom.security

import de.thm.mni.ii.classroom.properties.DownstreamGateway
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authorization.AuthorizationContext
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache
import reactor.core.publisher.Mono

@Configuration
class SecurityConfiguration(private val downstreamGateway: DownstreamGateway) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .httpBasic().disable()
            .logout().disable()
            .formLogin().disable()
            .authorizeExchange().pathMatchers("/api/*").access(this::authorizationManagerBBBToken)
            .and().build()
    }

    fun authorizationManagerBBBToken(authentication: Mono<Authentication>, context: AuthorizationContext): Mono<AuthorizationDecision> {
        val exchange = context.exchange
        val query = exchange.request.uri.query?.replace(Regex("&checksum=\\w+"), "") ?: ""
        val apiCall = exchange.request.uri.toString().substringAfterLast("/").substringBefore("?")
        logger.debug("calculating from $apiCall$query${downstreamGateway.sharedSecret}")
        val calculatedHash = DigestUtils.sha1Hex("$apiCall$query${downstreamGateway.sharedSecret}")
        val givenHash = exchange.request.queryParams["checksum"]?.get(0) ?: ""
        logger.debug("calculated: $calculatedHash, given: $givenHash")
        return Mono.just(AuthorizationDecision(calculatedHash == givenHash))
    }

    @Bean
    fun reactiveUserDetailsService(): ReactiveUserDetailsService {
        return ReactiveUserDetailsService { Mono.empty() }
    }
}


