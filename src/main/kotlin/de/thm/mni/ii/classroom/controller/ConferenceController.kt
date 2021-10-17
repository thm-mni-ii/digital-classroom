package de.thm.mni.ii.classroom.controller

import de.thm.mni.ii.classroom.event.InvitationEvent
import de.thm.mni.ii.classroom.model.classroom.ConferenceInfo
import de.thm.mni.ii.classroom.model.classroom.JoinLink
import de.thm.mni.ii.classroom.model.classroom.UserCredentials
import de.thm.mni.ii.classroom.services.ConferenceService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import reactor.core.publisher.Mono

@Controller
class ConferenceController(private val conferenceService: ConferenceService) {

    @MessageMapping("socket/conference/create")
    fun createConference(@AuthenticationPrincipal userCredentials: UserCredentials, @Payload conferenceInfo: ConferenceInfo): Mono<ConferenceInfo> {
        assert(userCredentials.classroomId == conferenceInfo.classroomId)
        return conferenceService.createConference(userCredentials, conferenceInfo)
    }

    @MessageMapping("socket/conference/join")
    fun joinConference(@AuthenticationPrincipal userCredentials: UserCredentials, @Payload conferenceInfo: ConferenceInfo): Mono<JoinLink> {
        assert(userCredentials.classroomId == conferenceInfo.classroomId)
        return conferenceService.joinConference(userCredentials, conferenceInfo)
    }

    @MessageMapping("socket/conference/leave")
    fun leaveConference(@AuthenticationPrincipal userCredentials: UserCredentials, @Payload conferenceInfo: ConferenceInfo): Mono<Void> {
        assert(userCredentials.classroomId == conferenceInfo.classroomId)
        return conferenceService.leaveConference(userCredentials, conferenceInfo)
    }

    @MessageMapping("socket/conference/end")
    fun endConference(@AuthenticationPrincipal userCredentials: UserCredentials, @Payload conferenceInfo: ConferenceInfo): Mono<Void> {
        assert(userCredentials.classroomId == conferenceInfo.classroomId)
        return conferenceService.endConference(userCredentials, conferenceInfo)
    }

    @MessageMapping("socket/conference/invite")
    fun inviteToConference(@AuthenticationPrincipal userCredentials: UserCredentials, @Payload invitationEvent: InvitationEvent): Mono<Void> {
        assert(userCredentials.classroomId == invitationEvent.getClassroomId())
        return conferenceService.forwardInvitation(userCredentials, invitationEvent)
    }
}
