package de.thm.mni.ii.classroom.controller

import de.thm.mni.ii.classroom.event.ClassroomEvent
import de.thm.mni.ii.classroom.model.classroom.User
import de.thm.mni.ii.classroom.services.ClassroomInstanceService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono

@Controller
class UserWebSocketController(
    private val classroomInstanceService: ClassroomInstanceService
) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @MessageMapping("socket/classroom")
    fun userStream(@AuthenticationPrincipal user: User, stream: Flux<ClassroomEvent>): Flux<ClassroomEvent> {
        return Flux.create {
            userConnected(user, it)
            stream.doOnCancel {
                logger.info("CANCEL")
                userDisconnected(user)
            }.doOnComplete {
                logger.info("COMPLETE")
                userDisconnected(user)
            }.doOnTerminate {
                logger.info("TERMINATE")
                userDisconnected(user)
            }.subscribe(::receiveEvent)
        }
    }

    private fun receiveEvent(event: ClassroomEvent): Mono<Void> {
        logger.info("Received event! ${event.javaClass}")
        return Mono.empty()
    }

    private fun userDisconnected(user: User) {
        logger.info("${user.userId} / ${user.fullName} disconnected!")
    }

    private fun userConnected(user: User, fluxSink: FluxSink<ClassroomEvent>) {
        classroomInstanceService.getClassroomInstance(user.classroomId).doOnNext { classroom ->
            logger.info("${user.userId} / ${user.fullName} connected to ${classroom.classroomName}!")
        }.subscribe { classroom ->
            classroom.connectSocket(user, fluxSink)
        }.dispose()
    }

}
