package de.thm.mni.ii.classroom.security.downstream

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

/**
 * Authentication object representing a downstream gateway request.
 * Credentials:
 * @param host: The host name or ip adress of the gateway.
 * @param givenChecksum: The checksum given with the gateways request.
 * @see DownstreamAPISecurity.downstreamAPIAuthenticationConverter
 */
class DownstreamAPIAuthentication(private val host: String, private val givenChecksum: String): Authentication {

    // The re-calculated checksum
    lateinit var calculatedChecksum: String
    // Boolean usable to invalidate the Authentication as specified by Spring framework.
    private var valid = true

    override fun getName(): String = host

    /**
     * Authority of this Authentication is always GATEWAY.
     * @see de.thm.mni.ii.classroom.security.SecurityConfiguration
     */
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> =
        mutableListOf(GrantedAuthority { "GATEWAY" })

    /**
     * The checkum given with the request.
     */
    override fun getCredentials(): String = givenChecksum

    /**
     * the calculated checksum
     */
    override fun getDetails(): String = calculatedChecksum

    override fun getPrincipal(): String = host

    /**
     * The request is authenticated when the givenChecksum matches the calculatedChecksum
     * and this Authentication has not been invalidated.
     * @return Whether the request is authenticated.
     */
    override fun isAuthenticated(): Boolean = givenChecksum == calculatedChecksum && valid

    /**
     * Method to invalidate the Authentication. May only be called with false.
     * @param isAuthenticated Boolean: false.
     * @exception IllegalArgumentException thrown when true is given as paramter.
     */
    override fun setAuthenticated(isAuthenticated: Boolean) {
        if (isAuthenticated) {
            throw IllegalArgumentException()
        } else {
            valid = false
        }
    }
}