package de.thm.mni.ii.classroom.security.downstream

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

class DownstreamAPIAuthentication(private val host: String, private val givenChecksum: String): Authentication {
        
        lateinit var calculatedChecksum: String
        private var valid = true
        
        override fun getName(): String = host

        override fun getAuthorities(): MutableCollection<out GrantedAuthority> =
            mutableListOf(GrantedAuthority { "GATEWAY" })

        override fun getCredentials(): String = givenChecksum

        override fun getDetails(): String = calculatedChecksum

        override fun getPrincipal(): String = host

        override fun isAuthenticated(): Boolean = givenChecksum == calculatedChecksum && valid

        override fun setAuthenticated(isAuthenticated: Boolean) {
            if (isAuthenticated) {
                throw IllegalArgumentException()
            } else {
                valid = false
            }
        }
    }