package de.thm.mni.ii.classroom.model.classroom

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import de.thm.mni.ii.classroom.config.ZonedDateTimeMillisDeserializer
import de.thm.mni.ii.classroom.config.ZonedDateTimeMillisSerializer
import de.thm.mni.ii.classroom.model.ClassroomDependent
import java.time.ZonedDateTime

data class Ticket(
    var description: String,
    val creator: User,
    var assignee: User? = null
): Comparable<Ticket>, ClassroomDependent {

    @JsonSerialize(using = ZonedDateTimeMillisSerializer::class)
    @JsonDeserialize(using = ZonedDateTimeMillisDeserializer::class)
    val createTime: ZonedDateTime = ZonedDateTime.now()
    override val classroomId = creator.classroomId

    override fun compareTo(other: Ticket): Int {
        return this.createTime.compareTo(other.createTime)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Ticket

        if (description != other.description) return false
        if (creator != other.creator) return false
        if (createTime != other.createTime) return false
        if (classroomId != other.classroomId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = description.hashCode()
        result = 31 * result + creator.hashCode()
        result = 31 * result + createTime.hashCode()
        result = 31 * result + classroomId.hashCode()
        return result
    }

}
