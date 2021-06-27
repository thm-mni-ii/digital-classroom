package de.thm.mni.ii.classroom.model

import org.springframework.security.core.userdetails.UserDetails
import java.security.Principal

data class User(
    val userId: String,
    val fullName: String,
    val classroomId: String
): Principal, UserDetails {

    private val userRole: UserRole = UserRole.STUDENT

    fun getRole() = userRole

    fun hasRole(role: UserRole, vararg roles: UserRole) =
        userRole == role || roles.contains(userRole)

    override fun getName(): String = userId
    override fun getAuthorities() = mutableSetOf(this.userRole)
    override fun getPassword() = ""
    override fun getUsername() = userId
    override fun isAccountNonExpired() = true
    override fun isAccountNonLocked() = true
    override fun isCredentialsNonExpired() = true
    override fun isEnabled() = true

}
