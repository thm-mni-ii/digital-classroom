package de.thm.mni.ii.classroom.security.jwt

import de.thm.mni.ii.classroom.model.classroom.User
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ClassroomSessionTokenRepository : ReactiveUserDetailsService {

    private val sessionTokens = HashMap<String, User>()

    fun authenticateBySessionToken(sessionToken: String): Mono<User> =
        Mono.justOrEmpty(sessionTokens.remove(sessionToken))

    fun insertValidToken(sessionToken: String, user: User) {
        sessionTokens[sessionToken] = user
    }

    override fun findByUsername(username: String): Mono<UserDetails> {
        return authenticateBySessionToken(username).cast(UserDetails::class.java)
    }
}
