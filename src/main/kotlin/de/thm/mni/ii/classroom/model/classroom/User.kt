package de.thm.mni.ii.classroom.model.classroom

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import de.thm.mni.ii.classroom.model.ClassroomDependent
import org.springframework.security.core.userdetails.UserDetails
import java.security.Principal

@JsonIgnoreProperties(ignoreUnknown = true)
open class User(
    override val classroomId: String,
    val userId: String,
    val fullName: String,
    var userRole: UserRole
) : Principal, UserDetails, ClassroomDependent {

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
        Pair("classroomId", classroomId),
        Pair("userId", userId),
        Pair("fullName", fullName),
        Pair("userRole", userRole.name)
    )

    @JsonIgnore
    constructor(claims: Map<String, Any>) : this(
        claims["classroomId"] as String,
        claims["userId"] as String,
        claims["fullName"] as String,
        UserRole.valueOf(claims["userRole"] as String)
    )

    @JsonIgnore
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (classroomId != other.classroomId) return false
        if (userId != other.userId) return false

        return true
    }

    @JsonIgnore
    override fun hashCode(): Int {
        var result = classroomId.hashCode()
        result = 31 * result + userId.hashCode()
        return result
    }
}
