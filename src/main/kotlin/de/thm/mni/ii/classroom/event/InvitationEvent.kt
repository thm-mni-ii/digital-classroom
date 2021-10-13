package de.thm.mni.ii.classroom.event

import de.thm.mni.ii.classroom.model.classroom.ConferenceInfo
import de.thm.mni.ii.classroom.model.classroom.UserCredentials

data class InvitationEvent(
    val inviter: UserCredentials,
    val invitee: UserCredentials,
    val conferenceInfo: ConferenceInfo
) : ClassroomEvent(InvitationEvent::class.simpleName!!)
