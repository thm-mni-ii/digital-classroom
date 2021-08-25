package de.thm.mni.ii.classroom.controller

import com.fasterxml.jackson.databind.JsonNode
import de.thm.mni.ii.classroom.event.ClassroomEvent
import de.thm.mni.ii.classroom.model.classroom.User
import de.thm.mni.ii.classroom.services.ClassroomInstanceService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.codec.CharSequenceEncoder
import org.springframework.core.codec.Encoder
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.rsocket.PayloadUtils
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.annotation.ConnectMapping
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import java.time.Duration

@Controller
class UserWebSocketController(
    private val classroomInstanceService: ClassroomInstanceService
) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private fun userConnected(user: User) {
        classroomInstanceService.getClassroomInstance(user.classroomId).doOnNext { classroom ->
            logger.info("${user.userId} / ${user.fullName} connected to ${classroom.classroomName}!")
        }.subscribe()
    }

    @MessageMapping("socket/classroom")
    fun userStream(@AuthenticationPrincipal user: User, requester: RSocketRequester): Flux<Int> {
        return Flux.create<Int?> {
            userConnected(user, it)
            requester
                .rsocket()!!
                .fireAndForget(PayloadUtils.createPayload(DefaultDataBufferFactory.sharedInstance.wrap("test".toByteArray()))).subscribe()
        }.doOnCancel {
            logger.info("CANCEL")
            userDisconnected(user)
        }.doOnComplete {
            logger.info("COMPLETE")
            userDisconnected(user)
        }
    }

    @MessageMapping("socket/client")
    fun receiveEvent(@AuthenticationPrincipal user: User, @Payload event: String) {
        logger.info("Received event! $event")
    }

    private fun userDisconnected(user: User) {
        logger.info("${user.userId} / ${user.fullName} disconnected!")
    }

    private fun userConnected(user: User, fluxSink: FluxSink<Int>) {
        classroomInstanceService.getClassroomInstance(user.classroomId).doOnNext { classroom ->
            logger.info("${user.userId} / ${user.fullName} connected to ${classroom.classroomName}!")
        }.subscribe { classroom ->
            //classroom.connectSocket(user, fluxSink)
            Flux.fromIterable(1..20).delayElements(Duration.ofSeconds(1)).subscribe {
                fluxSink.next(it)
            }
        }.dispose()
    }

}
