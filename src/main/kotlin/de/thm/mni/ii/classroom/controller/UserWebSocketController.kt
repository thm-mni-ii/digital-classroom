package de.thm.mni.ii.classroom.controller

import de.thm.mni.ii.classroom.event.ClassroomEvent
import de.thm.mni.ii.classroom.model.classroom.ClassroomInfo
import de.thm.mni.ii.classroom.model.classroom.ConferenceInfo
import de.thm.mni.ii.classroom.model.classroom.Ticket
import de.thm.mni.ii.classroom.model.classroom.UserCredentials
import de.thm.mni.ii.classroom.model.classroom.User
import de.thm.mni.ii.classroom.services.ClassroomEventReceiverService
import de.thm.mni.ii.classroom.services.ClassroomUserService
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
    private val userService: ClassroomUserService,
    private val classroomEventReceiverService: ClassroomEventReceiverService
) {
    @ConnectMapping
    fun connect(@AuthenticationPrincipal userCredentials: UserCredentials, requester: RSocketRequester): Mono<Void> {
        return userService.userConnected(userCredentials, requester)
    }

    @MessageMapping("socket/classroom-event")
    fun receiveEvent(@AuthenticationPrincipal userCredentials: UserCredentials, @Payload event: ClassroomEvent) {
        classroomEventReceiverService.classroomEventReceived(userCredentials, event)
    }

    @MessageMapping("socket/init-classroom")
    fun initClassroom(@AuthenticationPrincipal userCredentials: UserCredentials): Mono<ClassroomInfo> {
        return userService.getClassroomInfo(userCredentials)
    }

    @MessageMapping("socket/init-tickets")
    fun initTickets(@AuthenticationPrincipal userCredentials: UserCredentials): Flux<Ticket> {
        return userService.getTickets(userCredentials)
    }

    @MessageMapping("socket/init-users")
    fun initUsers(@AuthenticationPrincipal userCredentials: UserCredentials): Flux<User> {
        return userService.getUserDisplays(userCredentials)
    }

    @MessageMapping("socket/init-conferences")
    fun initConferences(@AuthenticationPrincipal userCredentials: UserCredentials): Flux<ConferenceInfo> {
        return userService.getConferences(userCredentials)
    }
}
