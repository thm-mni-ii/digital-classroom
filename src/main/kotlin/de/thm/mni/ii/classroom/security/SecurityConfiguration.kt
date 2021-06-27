package de.thm.mni.ii.classroom.security

import de.thm.mni.ii.classroom.properties.DownstreamGatewayProperties
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authorization.AuthorizationContext
import reactor.core.publisher.Mono

@EnableWebFluxSecurity
class SecurityConfiguration(private val downstreamGatewayProperties: DownstreamGatewayProperties) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http.httpBasic().disable()
        http.logout().disable()
        http.formLogin().disable()
        http.authorizeExchange().pathMatchers("/classroom").permitAll()
        http.authorizeExchange().pathMatchers("/api/*").access(this::authorizationManagerBBBToken)
        return http.build()
    }

    fun authorizationManagerBBBToken(authentication: Mono<Authentication>, context: AuthorizationContext): Mono<AuthorizationDecision> {
        val exchange = context.exchange
        val query = exchange.request.uri.rawQuery?.replace(Regex("&checksum=\\w+"), "") ?: ""
        val apiCall = exchange.request.uri.toString().substringAfterLast("/").substringBefore("?")
        logger.debug("calculating from $apiCall$query${downstreamGatewayProperties.sharedSecret}")
        val calculatedHash = DigestUtils.sha1Hex("$apiCall$query${downstreamGatewayProperties.sharedSecret}")
        val givenHash = exchange.request.queryParams["checksum"]?.get(0) ?: ""
        logger.debug("calculated: $calculatedHash, given: $givenHash")
        return Mono.just(AuthorizationDecision(calculatedHash == givenHash))
    }

    @Bean
    fun reactiveUserDetailsService(): ReactiveUserDetailsService {
        return ReactiveUserDetailsService { Mono.empty() }
    }
}


