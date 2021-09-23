package de.thm.mni.ii.classroom.model.classroom

import de.thm.mni.ii.classroom.model.ClassroomDependent

data class UserDisplay(
    override val classroomId: String,
    val userId: String,
    val fullName: String,
    val userRole: UserRole,
    var inConference: Boolean = false,
    var conferenceId: String?
) : ClassroomDependent {
    constructor(user: User, conference: Conference?) : this(
        user.classroomId,
        user.userId,
        user.fullName,
        user.userRole,
        conference?.visible ?: false,
        if (conference?.visible == true) conference.conferenceId else null
    )
}
