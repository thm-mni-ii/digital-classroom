package de.thm.mni.ii.classroom.model.classroom

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import de.thm.mni.ii.classroom.config.ZonedDateTimeMillisDeserializer
import de.thm.mni.ii.classroom.config.ZonedDateTimeMillisSerializer
import de.thm.mni.ii.classroom.model.ClassroomDependent
import java.time.ZonedDateTime

class ConferenceInfo(
    override val classroomId: String,
    val conferenceId: String?,
    val conferenceName: String,
    val creator: User?,
    val visible: Boolean,
    @field:JsonSerialize(using = ZonedDateTimeMillisSerializer::class)
    @field:JsonDeserialize(using = ZonedDateTimeMillisDeserializer::class)
    val creation: ZonedDateTime?
): ClassroomDependent {
    constructor(conference: Conference): this(
        conference.classroomId,
        conference.conferenceId,
        conference.conferenceName,
        conference.creator,
        conference.visible,
        conference.creation
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConferenceInfo

        if (classroomId != other.classroomId) return false
        if (conferenceId != other.conferenceId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = classroomId.hashCode()
        result = 31 * result + (conferenceId?.hashCode() ?: 0)
        return result
    }

}

data class JoinLink(val url: String)
