package de.thm.mni.ii.classroom.security.classroom

import de.thm.mni.ii.classroom.model.classroom.User
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ClassroomUserDetailsRepository: ReactiveUserDetailsService {

    private val validTokens = HashMap<String, User>()

    fun findBySessionToken(sessionToken: String): User? =
        validTokens.get(sessionToken)

    fun insertValidToken(sessionToken: String, user: User) {
        validTokens[sessionToken] = user
    }

    override fun findByUsername(username: String): Mono<UserDetails?> {
        return Mono.justOrEmpty(findBySessionToken(username))
    }

}