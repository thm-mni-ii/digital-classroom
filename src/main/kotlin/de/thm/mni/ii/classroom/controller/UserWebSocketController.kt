package de.thm.mni.ii.classroom.controller

import de.thm.mni.ii.classroom.model.classroom.User
import de.thm.mni.ii.classroom.socket.UserSocketService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import java.time.Instant

@Controller
class UserWebSocketController(
    private val userSocketService: UserSocketService
) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private val messages: HashSet<Message> = HashSet<Message>().apply { this.add(Message("test", "test")) }

    @MessageMapping("stream/users")
    fun userStream(@AuthenticationPrincipal auth: User): Flux<Any> {
        logger.info(auth.fullName)
        return Flux.empty()
    }

    @MessageMapping("stream/tickets")
    fun messageStream(): Flux<Message> = this.messages.toFlux().doOnNext {
        logger.info(it.body)
    }

    @MessageMapping("stream/invites")
    fun inviteStream(): Flux<Message> = this.messages.toFlux().doOnNext {
        logger.info(it.body)
    }

    data class Message(var id: String? = null, var body: String, var sentAt: Instant = Instant.now())

}
