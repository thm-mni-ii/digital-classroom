package de.thm.mni.ii.classroom.controller

import de.thm.mni.ii.classroom.event.ClassroomEvent
import de.thm.mni.ii.classroom.event.MessageEvent
import de.thm.mni.ii.classroom.event.UserEvent
import de.thm.mni.ii.classroom.model.classroom.User
import de.thm.mni.ii.classroom.services.ClassroomInstanceService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.annotation.ConnectMapping
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import reactor.core.publisher.Mono

@Controller
class UserWebSocketController(
    private val classroomInstanceService: ClassroomInstanceService
) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @ConnectMapping
    fun connect(@AuthenticationPrincipal user: User, requester: RSocketRequester): Mono<Void> {
        return Mono.defer {
            userConnected(user, requester)
            val socket = requester.rsocket()!!
            socket.onClose()
                .doOnSuccess {
                    userDisconnected(user)
                }.doOnError {

                }
            Mono.empty()
        }

    }

    @MessageMapping("socket/classroom-event")
    fun receiveEvent(@AuthenticationPrincipal user: User, @Payload event: ClassroomEvent) {
        when (event) {
            is MessageEvent -> logger.info("Received Message: ${event.message}")
            else -> logger.info("Received unknown event! ${event.javaClass.name}")
        }
    }

    private fun userDisconnected(user: User) {
        logger.info("${user.userId} / ${user.fullName} disconnected!")
    }

    private fun userDisconnected(user: User, throwable: Throwable) {
        logger.error("${user.userId} / ${user.fullName} disconnected with error!", throwable)
    }

    private fun userConnected(user: User, socketRequester: RSocketRequester) {
        classroomInstanceService.getClassroomInstance(user.classroomId).doOnNext { classroom ->
            logger.info("${user.userId} / ${user.fullName} connected to ${classroom.classroomName}!")
        }.subscribe { classroom ->
            classroom.connectSocket(user, socketRequester)
        }.dispose()
    }

}
