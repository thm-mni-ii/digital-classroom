package de.thm.mni.ii.classroom.event

import de.thm.mni.ii.classroom.model.classroom.User

data class UserEvent(
    val user: User,
    val inConference: Boolean = false,
    val conferenceId: String? = null,
    val userAction: UserAction
) : ClassroomEvent(UserEvent::class.simpleName!!)

enum class UserAction {
    JOIN,
    JOIN_CONFERENCE,
    LEAVE_CONFERENCE,
    LEAVE
}
