package de.thm.mni.ii.classroom.controller

import de.thm.mni.ii.classroom.event.ClassroomEvent
import de.thm.mni.ii.classroom.event.MessageEvent
import de.thm.mni.ii.classroom.event.TicketEvent
import de.thm.mni.ii.classroom.event.UserEvent
import de.thm.mni.ii.classroom.model.classroom.User
import de.thm.mni.ii.classroom.services.ClassroomInstanceService
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
    private val classroomInstanceService: ClassroomInstanceService
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

    @MessageMapping("socket/init-tickets")
    fun initTickets(@AuthenticationPrincipal user: User): Flux<TicketEvent> {
        return Flux.empty()
    }

    @MessageMapping("socket/init-users")
    fun initUsers(@AuthenticationPrincipal user: User): Flux<UserEvent> {
        return Flux.empty()
    }

    @MessageMapping("socket/init-conferences")
    fun initConferences(@AuthenticationPrincipal user: User): Flux<UserEvent> {
        return Flux.empty()
    }

    private fun userDisconnected(user: User) {
        logger.logThread("userDisconnected")
        logger.info("${user.userId} / ${user.fullName} disconnected!")
    }

    private fun userDisconnected(user: User, throwable: Throwable) {
        logger.logThread("userDisconnected")
        logger.error("${user.userId} / ${user.fullName} disconnected with error!", throwable.message)
    }

    private fun userConnected(user: User, socketRequester: RSocketRequester) {
        classroomInstanceService.getClassroomInstance(user.classroomId).doOnNext { classroom ->
            logger.logThread("userConnected")
            logger.info("${user.userId} / ${user.fullName} connected to ${classroom.classroomName}!")
        }.subscribe { classroom ->
            classroom.connectSocket(user, socketRequester)
        }.dispose()
    }

}
