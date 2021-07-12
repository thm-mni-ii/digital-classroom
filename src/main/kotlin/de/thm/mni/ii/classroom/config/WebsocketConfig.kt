package de.thm.mni.ii.classroom.config

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import de.thm.mni.ii.classroom.event.ClassroomEvent
import de.thm.mni.ii.classroom.event.UserEventPublisher
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter
import reactor.core.publisher.Flux
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
    fun handlerMapping(userHandler: WebSocketHandler): HandlerMapping {
        return object : SimpleUrlHandlerMapping() {
            init {
                urlMap = Collections.singletonMap("/websocket/users", userHandler)
                order = 10
            }
        }
    }

    @Bean
    fun webSocketHandlerAdapter(): WebSocketHandlerAdapter {
        return WebSocketHandlerAdapter()
    }

    @Bean
    fun userHandler(objectMapper: ObjectMapper, eventPublisher: UserEventPublisher): WebSocketHandler {
        val publish: Flux<ClassroomEvent> = Flux
            .create(eventPublisher)
            .share()
        val logger = LoggerFactory.getLogger("UserWebsocketHandler")

        return WebSocketHandler { session: WebSocketSession ->
            logger.info("Connected ${session.handshakeInfo.principal.block()?.name}")
            val messageFlux = publish
                .map { evt: ClassroomEvent ->
                    try {
                        objectMapper.writeValueAsString(evt.source)
                    } catch (e: JsonProcessingException) {
                        throw RuntimeException(e)
                    }
                }
                .map { str: String ->
                    logger.info("sending $str")
                    session.textMessage(str)
                }
            session.send(messageFlux)
        }
    }
}