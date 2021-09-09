package de.thm.mni.ii.classroom.model.classroom

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import de.thm.mni.ii.classroom.config.ZonedDateTimeMillisDeserializer
import de.thm.mni.ii.classroom.config.ZonedDateTimeMillisSerializer
import de.thm.mni.ii.classroom.model.ClassroomDependent
import java.time.ZonedDateTime

class Ticket(
    override val classroomId: String,
    var description: String,
    val creator: User,
    var assignee: User? = null
): Comparable<Ticket>, ClassroomDependent {

    var ticketId: Long? = null

    @JsonSerialize(using = ZonedDateTimeMillisSerializer::class)
    @JsonDeserialize(using = ZonedDateTimeMillisDeserializer::class)
    val createTime: ZonedDateTime = ZonedDateTime.now()

    override fun compareTo(other: Ticket): Int {
        return this.createTime.compareTo(other.createTime)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Ticket

        if (creator != other.creator) return false
        if (ticketId != other.ticketId) return false
        if (createTime != other.createTime) return false
        if (classroomId != other.classroomId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = creator.hashCode()
        result = 31 * result + ticketId.hashCode()
        result = 31 * result + createTime.hashCode()
        result = 31 * result + classroomId.hashCode()
        return result
    }


}
