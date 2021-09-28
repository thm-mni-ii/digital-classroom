package de.thm.mni.ii.classroom.model.classroom

import com.fasterxml.jackson.annotation.JsonIgnore

class UserDisplay(
    classroomId: String,
    userId: String,
    fullName: String,
    userRole: UserRole,
    var visible: Boolean,
    val conferences: List<ConferenceInfo> = listOf()
) : User(classroomId, userId, fullName, userRole) {
    constructor(user: User, visible: Boolean) : this(
        user.classroomId,
        user.userId,
        user.fullName,
        user.userRole,
        visible
    )

    @JsonIgnore
    fun getUser(): User = this

}
