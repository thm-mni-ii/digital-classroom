package de.thm.mni.ii.classroom.event

import com.fasterxml.jackson.annotation.JsonIgnore
import de.thm.mni.ii.classroom.model.classroom.ConferenceInfo
import de.thm.mni.ii.classroom.model.classroom.UserCredentials

data class InvitationEvent(
    val inviter: UserCredentials,
    val invitee: UserCredentials,
    val conferenceInfo: ConferenceInfo
) : ClassroomEvent(InvitationEvent::class.simpleName!!) {
    @JsonIgnore
    override fun getClassroomId(): String {
        return if (inviter.classroomId !== invitee.classroomId || inviter.classroomId !== conferenceInfo.classroomId) {
            "INVALID"
        } else {
            this.conferenceInfo.classroomId
        }
    }
}
