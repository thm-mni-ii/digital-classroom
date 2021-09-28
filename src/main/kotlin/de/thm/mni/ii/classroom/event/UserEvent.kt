package de.thm.mni.ii.classroom.event

import de.thm.mni.ii.classroom.model.classroom.UserDisplay

data class UserEvent(
    val user: UserDisplay,
    val userAction: UserAction
) : ClassroomEvent(UserEvent::class.simpleName!!)

enum class UserAction {
    JOIN,
    VISIBILITY_CHANGE,
    LEAVE
}
