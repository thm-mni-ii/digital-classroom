package de.thm.mni.ii.classroom.controller

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toFlux
import java.time.Instant

@Controller
class UserWebSocketController {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private val messages: HashSet<Message> = HashSet<Message>().apply { this.add(Message("test", "test")) }

    @MessageMapping("send")
    fun hello(p: String) {
        this.messages.add(Message(body = p, sentAt = Instant.now()))
        logger.info(p)
    }

    @MessageMapping("messages")
    fun messageStream(): Flux<Message> = this.messages.toFlux().doOnNext {
        logger.info(it.body)
    }

    data class Message(var id: String? = null, var body: String, var sentAt: Instant = Instant.now())

}
