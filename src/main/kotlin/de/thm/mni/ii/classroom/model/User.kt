package de.thm.mni.ii.classroom.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.security.core.userdetails.UserDetails
import java.security.Principal

data class User(
    val userId: String,
    val fullName: String,
    val classroomId: String,
    var userRole: UserRole
): Principal, UserDetails {
    @JsonIgnore
    override fun getName(): String = fullName
    @JsonIgnore
    override fun getAuthorities() = mutableSetOf(this.userRole)
    @JsonIgnore
    override fun getPassword() = ""
    @JsonIgnore
    override fun getUsername() = userId
    @JsonIgnore
    override fun isAccountNonExpired() = true
    @JsonIgnore
    override fun isAccountNonLocked() = true
    @JsonIgnore
    override fun isCredentialsNonExpired() = true
    @JsonIgnore
    override fun isEnabled() = true
}
