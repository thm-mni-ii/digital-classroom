package de.thm.mni.ii.classroom.model

import java.util.*

data class Conference(
    val conferenceId: String,
    val classroomId: String,
    val conferenceName: String,
    val attendeePassword: String,
    val moderatorPassword: String
) {
    constructor(digitalClassroom: DigitalClassroom): this(
        UUID.randomUUID().toString(),
        digitalClassroom.classroomId,
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString()
    )
}