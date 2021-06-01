package de.thm.mni.ii.classroom.model

import java.security.Principal

data class User(
    private val userId: String,
    private val fullName: String,
    private val userRole: UserRole,
): Principal {

    override fun getName(): String = userId

    fun hasRole(role: UserRole, vararg roles: UserRole): Boolean {
        return userRole == role || roles.contains(userRole)
    }

}
