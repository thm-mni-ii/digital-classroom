package de.thm.mni.ii.classroom.model.classroom

import java.time.ZonedDateTime

class Conference(
    val classroomId: String,
    val conferenceId: String,
    val conferenceName: String,
    val attendeePassword: String,
    val moderatorPassword: String,
    val creator: User,
    var visible: Boolean,
    val attendees: MutableSet<User>,
    val creationTimestamp: ZonedDateTime = ZonedDateTime.now()
) {
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

    fun toConferenceInfo() = ConferenceInfo(this)

    fun removeUser(user: User): Conference {
        this.attendees.remove(user)
        return this
    }
}
