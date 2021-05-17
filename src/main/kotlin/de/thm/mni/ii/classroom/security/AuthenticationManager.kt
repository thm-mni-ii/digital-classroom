package de.thm.mni.ii.classroom.security

import de.thm.mni.ii.classroom.properties.DownstreamGateway
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.Authentication
import reactor.core.publisher.Mono

class AuthenticationManager(private val downstreamGateway: DownstreamGateway) : ReactiveAuthenticationManager {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        logger.debug("Shared Secret: ${downstreamGateway.sharedSecret}")
        val principal = authentication.principal as String?
        logger.debug("Checksum: $principal")
        val calculatedToken = authentication.credentials as String?
        logger.debug("Calculated: $calculatedToken")

        if (calculatedToken != principal) {
            throw BadCredentialsException("Calculated checksum does not equal given checksum! Wrong shared secret?")
        }
        authentication.isAuthenticated = true
        return Mono.just(authentication)
    }
}
