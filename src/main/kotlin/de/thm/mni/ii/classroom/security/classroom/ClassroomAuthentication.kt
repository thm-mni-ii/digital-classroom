package de.thm.mni.ii.classroom.security.classroom

import de.thm.mni.ii.classroom.model.User
import org.springframework.security.core.Authentication

class ClassroomAuthentication(private val user: User?,
                              private val jwt: String?,
                              private val sessionToken: String?): Authentication {

    private var valid: Boolean = true

    override fun getName() = user?.name

    override fun getAuthorities() = user?.authorities

    override fun getCredentials() = jwt ?: sessionToken

    override fun getDetails() = jwt

    override fun getPrincipal() = user!!

    override fun isAuthenticated() = user != null && valid

    override fun setAuthenticated(isAuthenticated: Boolean) {
        valid = isAuthenticated
    }


}