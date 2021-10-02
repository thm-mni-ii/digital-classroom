package de.thm.mni.ii.classroom.security.jwt

import de.thm.mni.ii.classroom.model.classroom.User
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ClassroomTokenRepository {

    private val sessionTokens = HashMap<String, User>()
    private val refreshTokens = HashMap<String, User>()

    fun authenticateBySessionToken(sessionToken: String): Mono<User> =
        Mono.justOrEmpty(sessionTokens.remove(sessionToken))

    fun insertSessionToken(sessionToken: String, user: User) {
        sessionTokens[sessionToken] = user
    }

    fun findRefreshToken(refreshToken: String): Mono<User> =
        Mono.justOrEmpty(refreshTokens[refreshToken])

    fun insertRefreshToken(refreshToken: String, user: User) {
        refreshTokens[refreshToken] = user
    }
}
