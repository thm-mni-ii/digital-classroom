package de.thm.mni.ii.classroom.event

import com.fasterxml.jackson.annotation.JsonIgnore
import de.thm.mni.ii.classroom.model.classroom.User

data class UserEvent(
    val user: User,
    val userAction: UserAction
) : ClassroomEvent(UserEvent::class.simpleName!!) {
    @JsonIgnore override fun getClassroomId(): String = this.user.classroomId
}

enum class UserAction {
    JOIN,
    VISIBILITY_CHANGE,
    LEAVE
}
