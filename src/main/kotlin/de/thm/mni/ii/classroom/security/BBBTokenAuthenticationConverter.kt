package de.thm.mni.ii.classroom.security

import de.thm.mni.ii.classroom.properties.DownstreamGateway
import org.apache.commons.codec.digest.DigestUtils
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.web.authentication.AuthenticationConverter
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import javax.servlet.http.HttpServletRequest

class BBBTokenAuthenticationConverter(private val downstreamGateway: DownstreamGateway) : ServerAuthenticationConverter {

    override fun convert(exchange: ServerWebExchange): Mono<Authentication> {
        val query = exchange.request.uri.query.replace("&checksum=\\s+", "")
        val apiCall = exchange.request.uri.toString().substringAfterLast("/")
        DigestUtils.sha1Hex("$apiCall$query${downstreamGateway.sharedSecret}")
        return Mono.just(createAuth())
    }

    private fun createAuth(): Authentication {
        TODO()
    }
}

class BBBSharedSecretAuthentication() : AbstractAuthenticationToken(null) {
    override fun getPrincipal(): Any = "test"

    override fun getCredentials(): Any = "dummy"

}
