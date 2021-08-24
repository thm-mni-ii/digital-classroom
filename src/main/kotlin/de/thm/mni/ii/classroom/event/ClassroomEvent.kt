package de.thm.mni.ii.classroom.event

import de.thm.mni.ii.classroom.model.ClassroomDependent
import de.thm.mni.ii.classroom.model.classroom.ConferenceInfo
import de.thm.mni.ii.classroom.model.classroom.Ticket
import de.thm.mni.ii.classroom.model.classroom.User
import org.springframework.context.ApplicationEvent

abstract class ClassroomEvent(source: ClassroomDependent): ApplicationEvent(source)

data class UserEvent(
    val user: User,
    val joined: Boolean,
    val inConference: Boolean,
    val conferenceId: String?
): ClassroomEvent(user)

data class TicketEvent(
    val ticket: Ticket,
    val open: Boolean
): ClassroomEvent(ticket)

data class ConferenceEvent(
    val conferenceInfo: ConferenceInfo,
    val inProgress: Boolean
): ClassroomEvent(conferenceInfo)

data class InvitationEvent(
    val inviter: User,
    val invitee: User,
    val conferenceInfo: ConferenceInfo,
    val joinLink: String
): ClassroomEvent(inviter)
