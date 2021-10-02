package de.thm.mni.ii.classroom.security.jwt

import de.thm.mni.ii.classroom.model.classroom.User
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

class ClassroomRefreshTokenRepository : ReactiveUserDetailsService {

    private val refreshTokens = HashMap<String, User>()

    fun findRefreshToken(refreshToken: String): Mono<User> =
        Mono.justOrEmpty(refreshTokens[refreshToken])

    fun insertRefreshToken(refreshToken: String, user: User) {
        refreshTokens[refreshToken] = user
    }

    override fun findByUsername(username: String): Mono<UserDetails> {
        return findRefreshToken(username).cast(UserDetails::class.java)
    }
}
