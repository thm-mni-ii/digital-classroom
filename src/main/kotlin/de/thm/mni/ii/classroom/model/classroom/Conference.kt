package de.thm.mni.ii.classroom.model.classroom

import de.thm.mni.ii.classroom.model.ClassroomDependent
import java.time.ZonedDateTime

class Conference(
    override val classroomId: String,
    val conferenceId: String,
    val conferenceName: String,
    val attendeePassword: String,
    val moderatorPassword: String,
    val creator: UserCredentials,
    var visible: Boolean,
    val attendees: LinkedHashSet<UserCredentials>,
    val creationTimestamp: ZonedDateTime = ZonedDateTime.now(),
) : ClassroomDependent {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Conference

        if (classroomId != other.classroomId) return false
        if (conferenceId != other.conferenceId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = classroomId.hashCode()
        result = 31 * result + conferenceId.hashCode()
        return result
    }

    override fun toString(): String {
        return "$conferenceId / $conferenceName"
    }

    fun toConferenceInfo(): ConferenceInfo {
        return ConferenceInfo(this)
    }

    fun removeUser(userCredentials: UserCredentials): Conference {
        this.attendees.remove(userCredentials)
        return this
    }
}
