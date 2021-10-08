package de.thm.mni.ii.classroom.event

import de.thm.mni.ii.classroom.model.classroom.ConferenceInfo
import de.thm.mni.ii.classroom.model.classroom.User

data class InvitationEvent(
    val inviter: User,
    val invitee: User,
    val conferenceInfo: ConferenceInfo
) : ClassroomEvent(InvitationEvent::class.simpleName!!)
