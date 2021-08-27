package de.thm.mni.ii.classroom.model.classroom

import de.thm.mni.ii.classroom.model.ClassroomDependent
import java.util.*
import kotlin.collections.HashSet

class Conference(
    conferenceId: String,
    override val classroomId: String,
    conferenceName: String,
    val attendeePassword: String,
    val moderatorPassword: String,
    creator: User,
    visible: Boolean,
    attendees: HashSet<User>
): ConferenceInfo(classroomId, conferenceId, conferenceName, creator, visible, attendees) {
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

    fun getConferenceInformation(): ConferenceInfo {
        return ConferenceInfo(classroomId, conferenceId, conferenceName, creator, visible, attendees)
    }

}

