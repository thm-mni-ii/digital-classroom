package de.thm.mni.ii.classroom.controller

import de.thm.mni.ii.classroom.event.ClassroomEvent
import de.thm.mni.ii.classroom.event.MessageEvent
import de.thm.mni.ii.classroom.event.UserEvent
import de.thm.mni.ii.classroom.model.classroom.ClassroomInfo
import de.thm.mni.ii.classroom.model.classroom.ConferenceInfo
import de.thm.mni.ii.classroom.model.classroom.Ticket
import de.thm.mni.ii.classroom.model.classroom.User
import de.thm.mni.ii.classroom.services.ClassroomUserSocketService
import de.thm.mni.ii.classroom.util.logThread
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.annotation.ConnectMapping
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Controller
class UserWebSocketController(
    private val userSocketService: ClassroomUserSocketService
) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @ConnectMapping
    fun connect(@AuthenticationPrincipal user: User, requester: RSocketRequester): Mono<Void> {
        logger.logThread("connected")
        return Mono.defer {
            logger.logThread("deferredMono connected")
            userConnected(user, requester)
            val socket = requester.rsocket()!!
            socket.onClose()
                .doOnSuccess {
                    userDisconnected(user)
                }.doOnError {
                    userDisconnected(user, it)
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

    @MessageMapping("socket/init-classroom")
    fun initClassroom(@AuthenticationPrincipal user: User): Mono<ClassroomInfo> {
        return userSocketService.getClassroomInfo(user)
    }

    @MessageMapping("socket/init-tickets")
    fun initTickets(@AuthenticationPrincipal user: User): Flux<Ticket> {
        return userSocketService.getTickets(user)
    }

    @MessageMapping("socket/init-users")
    fun initUsers(@AuthenticationPrincipal user: User): Flux<UserEvent> {
        return Flux.empty()
    }

    @MessageMapping("socket/init-conferences")
    fun initConferences(@AuthenticationPrincipal user: User): Flux<ConferenceInfo> {
        return Flux.empty()
    }

    private fun userConnected(user: User, socketRequester: RSocketRequester) {
        logger.info("${user.userId} / ${user.fullName} connected to ${user.classroomId}!")
        userSocketService.userConnected(user, socketRequester)
    }

    private fun userDisconnected(user: User, throwable: Throwable? = null) {
        if (throwable == null) {
            logger.info("${user.userId} / ${user.fullName} disconnected from ${user.classroomId}!")
        } else {
            logger.error("${user.userId} / ${user.fullName} disconnected from ${user.classroomId} with error {}!", throwable.message)
        }
        userSocketService.userDisconnected(user)
    }

}
