package de.thm.mni.ii.classroom.model.classroom

class Conference(
    val classroomId: String,
    val conferenceId: String,
    val conferenceName: String,
    val attendeePassword: String,
    val moderatorPassword: String,
    val creator: User,
    val visible: Boolean,
)
