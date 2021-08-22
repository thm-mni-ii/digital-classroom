package de.thm.mni.ii.classroom.security.classroom

import de.thm.mni.ii.classroom.model.classroom.User
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.Authentication

class ClassroomAuthentication(
    val user: User?,
    private val jwt: String?
): AbstractAuthenticationToken(user?.authorities) {

    fun getClassroomId() = user!!.classroomId

    private var valid: Boolean = true

    override fun getName() = user?.name

    override fun getAuthorities() = user?.authorities

    override fun getCredentials() = jwt

    override fun getDetails() = jwt

    override fun getPrincipal() = user!!

    override fun isAuthenticated() = user != null && valid

    override fun setAuthenticated(isAuthenticated: Boolean) {
        valid = isAuthenticated
    }


}
