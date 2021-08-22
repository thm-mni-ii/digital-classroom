package de.thm.mni.ii.classroom.model.classroom

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.jsonwebtoken.Claims
import org.springframework.security.core.userdetails.UserDetails
import java.security.Principal

@JsonIgnoreProperties(ignoreUnknown = true)
data class User(
    val userId: String,
    val fullName: String,
    val classroomId: String,
    var userRole: UserRole
): Principal, UserDetails {

    fun isPrivileged(): Boolean = userRole == UserRole.TEACHER || userRole == UserRole.TUTOR

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

    @JsonIgnore
    fun getJwtClaims(): MutableMap<String, Any> = mutableMapOf(
        Pair("userId", userId),
        Pair("fullName", fullName),
        Pair("classroomId", classroomId),
        Pair("userRole", userRole.name)
    )

    @JsonIgnore
    constructor(claims: Claims) : this(
        claims["userId"] as String,
        claims["fullName"] as String,
        claims["classroomId"] as String,
        UserRole.valueOf(claims["userRole"] as String)
    )

    @JsonIgnore
    constructor(claims: Map<String, Any>) : this(
        claims["userId"] as String,
        claims["fullName"] as String,
        claims["classroomId"] as String,
        UserRole.valueOf(claims["userRole"] as String)
    )
}
