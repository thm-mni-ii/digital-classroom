package de.thm.mni.ii.classroom.model.classroom

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import de.thm.mni.ii.classroom.config.ZonedDateTimeMillisDeserializer
import de.thm.mni.ii.classroom.config.ZonedDateTimeMillisSerializer
import de.thm.mni.ii.classroom.model.ClassroomDependent
import java.time.ZonedDateTime

@Suppress("unused")
open class ConferenceInfo(
    override val classroomId: String,
    open val conferenceId: String?,
    val conferenceName: String,
    val creator: UserCredentials,
    var visible: Boolean,
    @field:JsonSerialize(using = ZonedDateTimeMillisSerializer::class)
    @field:JsonDeserialize(using = ZonedDateTimeMillisDeserializer::class)
    val creationTimestamp: ZonedDateTime,
    var attendeeIds: List<String>,
) : ClassroomDependent {

    constructor(conference: Conference) : this(
        conference.classroomId,
        conference.conferenceId,
        conference.conferenceName,
        conference.creator,
        conference.visible,
        conference.creationTimestamp,
        conference.attendees.map { it.userId },
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ConferenceInfo) return false

        if (classroomId != other.classroomId) return false
        if (conferenceId != other.conferenceId) return false
        if (creator != other.creator) return false

        return true
    }

    override fun hashCode(): Int {
        var result = classroomId.hashCode()
        result = 31 * result + (conferenceId?.hashCode() ?: 0)
        result = 31 * result + creator.hashCode()
        return result
    }
}
