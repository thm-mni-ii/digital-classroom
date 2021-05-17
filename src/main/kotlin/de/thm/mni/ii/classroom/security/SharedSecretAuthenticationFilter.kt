package de.thm.mni.ii.classroom.security

import de.thm.mni.ii.classroom.properties.DownstreamGateway
import org.apache.commons.codec.digest.DigestUtils
import org.springframework.context.annotation.Primary
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

class SharedSecretAuthenticationFilter(private val downstreamGateway: DownstreamGateway): AbstractPreAuthenticatedProcessingFilter() {

    override fun getPreAuthenticatedPrincipal(request: HttpServletRequest?): String = ""

    override fun getPreAuthenticatedCredentials(request: HttpServletRequest?): String {
        val queryString = request!!.queryString.replace("&checksum=\\s+", "")
        val apiCall = request.requestURI.substringAfterLast("/")
        return DigestUtils.sha1Hex("$apiCall$queryString${downstreamGateway.sharedSecret}")
    }

}
