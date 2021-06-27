package de.thm.mni.ii.classroom.model

import java.security.Principal

data class User(
    val userId: String,
    val fullName: String,
): Principal {

    var userRole: UserRole = UserRole.Student

    override fun getName(): String = userId

    fun getRole() = userRole

    fun hasRole(role: UserRole, vararg roles: UserRole): Boolean {
        return userRole == role || roles.contains(userRole)
    }

}
