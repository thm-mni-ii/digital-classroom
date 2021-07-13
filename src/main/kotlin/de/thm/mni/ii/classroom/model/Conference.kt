package de.thm.mni.ii.classroom.model

import java.util.*
import kotlin.collections.HashSet

data class Conference(
    val conferenceId: String,
    val classroomId: String,
    val conferenceName: String,
    val attendeePassword: String,
    val moderatorPassword: String,
    val creator: User,
    val visible: Boolean,
    val attendees: HashSet<User>
) {
    constructor(digitalClassroom: DigitalClassroom, creator: User): this(
        UUID.randomUUID().toString(),
        digitalClassroom.classroomId,
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        creator,
        true,
        HashSet()
    )

    fun fillAttendees(attendees: Collection<User>) {
        this.attendees.addAll(attendees)
    }

}