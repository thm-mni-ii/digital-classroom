package de.thm.mni.ii.classroom.model.classroom

import de.thm.mni.ii.classroom.model.ClassroomDependent
import java.util.*
import kotlin.collections.HashSet

data class Conference(
    val conferenceId: String,
    override val classroomId: String,
    val conferenceName: String,
    val attendeePassword: String,
    val moderatorPassword: String,
    val creator: User,
    val visible: Boolean,
    val attendees: HashSet<User>
): ClassroomDependent {
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

data class ConferenceInfo(
    override val classroomId: String,
    val conferenceId: String,
    val conferenceName: String,
    val creator: User,
    val visible: Boolean,
    val attendees: HashSet<User>,
): ClassroomDependent
