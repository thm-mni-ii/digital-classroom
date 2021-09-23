package de.thm.mni.ii.classroom.controller

import de.thm.mni.ii.classroom.event.ClassroomEvent
import de.thm.mni.ii.classroom.model.classroom.ClassroomInfo
import de.thm.mni.ii.classroom.model.classroom.ConferenceInfo
import de.thm.mni.ii.classroom.model.classroom.Ticket
import de.thm.mni.ii.classroom.model.classroom.User
import de.thm.mni.ii.classroom.model.classroom.UserDisplay
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
    fun connect(@AuthenticationPrincipal user: User, requester: RSocketRequester): Mono<Void> {
        return userService.userConnected(user, requester)
    }

    @MessageMapping("socket/classroom-event")
    fun receiveEvent(@AuthenticationPrincipal user: User, @Payload event: ClassroomEvent) {
        classroomEventReceiverService.classroomEventReceived(user, event)
    }

    @MessageMapping("socket/init-classroom")
    fun initClassroom(@AuthenticationPrincipal user: User): Mono<ClassroomInfo> {
        return userService.getClassroomInfo(user)
    }

    @MessageMapping("socket/init-tickets")
    fun initTickets(@AuthenticationPrincipal user: User): Flux<Ticket> {
        return userService.getTickets(user)
    }

    @MessageMapping("socket/init-users")
    fun initUsers(@AuthenticationPrincipal user: User): Flux<UserDisplay> {
        return userService.getUserDisplays(user)
    }

    @MessageMapping("socket/init-conferences")
    fun initConferences(@AuthenticationPrincipal user: User): Flux<ConferenceInfo> {
        return userService.getConferences(user)
    }
}
