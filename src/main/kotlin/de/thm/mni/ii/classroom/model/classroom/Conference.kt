package de.thm.mni.ii.classroom.model.classroom

import java.util.*

class Conference(
    val classroomId: String,
    val conferenceId: String,
    val conferenceName: String,
    val attendeePassword: String,
    val moderatorPassword: String,
    val creator: User,
    val visible: Boolean,
) {
    constructor(digitalClassroom: DigitalClassroom, creator: User): this(
        digitalClassroom.classroomId,
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        creator,
        true,
    )

}

