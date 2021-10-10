package de.thm.mni.ii.classroom.security.jwt

import de.thm.mni.ii.classroom.model.classroom.UserCredentials
import org.springframework.security.authentication.AbstractAuthenticationToken

class ClassroomAuthentication(
    val userCredentials: UserCredentials?,
    private val jwt: String?,
) : AbstractAuthenticationToken(userCredentials?.authorities) {

    fun getClassroomId() = userCredentials!!.classroomId

    private var valid: Boolean = true

    override fun getName() = userCredentials?.name

    override fun getAuthorities() = userCredentials?.authorities

    override fun getCredentials() = jwt!!

    override fun getDetails() = jwt

    override fun getPrincipal() = userCredentials!!

    override fun isAuthenticated() = userCredentials != null && valid

    override fun setAuthenticated(isAuthenticated: Boolean) {
        valid = isAuthenticated
    }
}
