package de.thm.mni.ii.classroom.security.classroom

import de.thm.mni.ii.classroom.security.jwt.ClassroomAuthentication
import de.thm.mni.ii.classroom.security.jwt.ClassroomJwtService
import de.thm.mni.ii.classroom.security.jwt.ClassroomTokenRepository
import de.thm.mni.ii.classroom.util.component1
import de.thm.mni.ii.classroom.util.component2
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
/**
 * Security component authenticating and authorizing requests at the classroom API using a single-use session token.
 * Basic mechanism: Users are pre-authenticating via /api/join by the GATEWAY service and placed inside the
 * ClassroomUserDetailsRepository with an associated session token.
 * This session token is sent by the client using the classroom and exchanged for a
 * Json Web Token (JWT) in the ServerAuthenticatedSuccessHandler.
 *
 * @param tokenRepository A DAO offering access to valid session token and UserDetails.
 * @param jwtService A service creating and verifying JWT with UserDetails.
 * @see ClassroomTokenRepository
 * @see ClassroomJwtService
 * @see de.thm.mni.ii.classroom.security.downstream.DownstreamAPISecurity
 */
@Component
class ClassroomHttpSessionTokenSecurity(
    private val tokenRepository: ClassroomTokenRepository,
    private val jwtService: ClassroomJwtService
) {
    /**
     * Function constructing the main {@link AuthenticationWebFilter}
     * Accumulates the ReactiveAuthenticationManager, ServerAuthenticationConverter, and ServerAuthenticationSuccessHandler
     * constrains the filter to requests at a specific path (/classroom/\**).
     * @see AuthenticationWebFilter
     * @see ReactiveAuthenticationManager
     * @see ServerAuthenticationConverter
     * @see ServerAuthenticationSuccessHandler
     */
    fun sessionTokenFilter(): AuthenticationWebFilter {
        val sessionTokenFilter: AuthenticationWebFilter
        val authManager = this.reactiveAuthenticationManager()
        sessionTokenFilter = AuthenticationWebFilter(authManager)
        sessionTokenFilter.setRequiresAuthenticationMatcher(
            AndServerWebExchangeMatcher(
                ServerWebExchangeMatchers.pathMatchers("/classroom-api/join"),
                ServerWebExchangeMatcher {
                    if (it.request.queryParams.containsKey("sessionToken")) {
                        ServerWebExchangeMatcher.MatchResult.match()
                    } else {
                        ServerWebExchangeMatcher.MatchResult.notMatch()
                    }
                }
            )
        )
        sessionTokenFilter.setServerAuthenticationConverter(this.sessionTokenAuthenticationConverter())
        return sessionTokenFilter
    }

    /**
     * The ReactiveAuthenticationManager validating the ClassroomAuthentication object.
     * Searches for the given sessionToken and creates a JWT if authenticated.
     * If the ClassroomAuthentication object is not valid, the JWTSecurity config will try to authenticate the request.
     * The Authentication object is constructed from the ServerExchange within the sessionTokenAuthenticationConverter.
     * @see ClassroomHttpSessionTokenSecurity.sessionTokenAuthenticationConverter
     * @see ReactiveAuthenticationManager
     * @see ClassroomHttpJwtSecurity
     * @see ClassroomAuthentication
     */
    private fun reactiveAuthenticationManager() = ReactiveAuthenticationManager { auth ->
        tokenRepository.authenticateBySessionToken(auth.credentials as String)
            .flatMap { user ->
                Mono.zip(Mono.just(user), jwtService.createToken(user))
            }.map { (user, token) ->
                ClassroomAuthentication(user, token)
            }.defaultIfEmpty(ClassroomAuthentication(null, null))
            .cast(Authentication::class.java)
    }

    /**
     *
     */
    private fun sessionTokenAuthenticationConverter() = ServerAuthenticationConverter { exchange ->
        Mono.just(
            BearerTokenAuthenticationToken(exchange.request.queryParams.getFirst("sessionToken"))
        )
    }
}
