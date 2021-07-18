package de.thm.mni.ii.classroom.config

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import de.thm.mni.ii.classroom.event.UserEventPublisher
import org.springframework.boot.logging.DeferredLog.replay
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors


@Configuration
class WebSocketConfiguration {

    @Bean
    fun executor(): Executor {
        return Executors.newSingleThreadExecutor()
    }

    @Bean
    fun webSocketHandlerAdapter(): WebSocketHandlerAdapter {
        return WebSocketHandlerAdapter()
    }

    @Bean
    fun webSocketMapping(mapper: ObjectMapper, eventPublisher: UserEventPublisher): HandlerMapping? {
        val map = mapOf("/websocket/users" to UserSocketHandler(mapper))
        return SimpleUrlHandlerMapping().apply {
            urlMap = map
            order = 10
        }
    }

    class UserSocketHandler(val mapper: ObjectMapper) : WebSocketHandler {
        val sink = Sinks.many().multicast().directBestEffort<Message>();
        val outputMessages: Flux<Message> = sink.asFlux();
        override fun handle(session: WebSocketSession): Mono<Void> {
            println("handling WebSocketSession...")
            session.receive()
                .map { it.payloadAsText }
                .map { Message(id= UUID.randomUUID().toString(), body = it, sentAt = Instant.now()) }
                .doOnNext { println(it) }
                .subscribe(
                    { message: Message -> sink.tryEmitNext(message) },
                    { error: Throwable -> sink.tryEmitError(error) }
                )
            return session.send(
                Mono.delay(Duration.ofMillis(100))
                    .thenMany(outputMessages.map { session.textMessage(toJson(it)) })
            )
        }
        fun toJson(message: Message): String = mapper.writeValueAsString(message)
    }
}

data class Message @JsonCreator constructor(
    @JsonProperty("id") var id: String? = null,
    @JsonProperty("body") var body: String,
    @JsonProperty("sentAt") var sentAt: Instant = Instant.now()
)