package de.thm.mni.ii.classroom.model.classroom

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import de.thm.mni.ii.classroom.config.ZonedDateTimeMillisDeserializer
import de.thm.mni.ii.classroom.config.ZonedDateTimeMillisSerializer
import java.time.ZonedDateTime

class Ticket: Comparable<Ticket> {
    var description: String? = null
    var creator: User? = null
    var assignee: User? = null
    var status: String? = null
    @JsonSerialize(using = ZonedDateTimeMillisSerializer::class)
    @JsonDeserialize(using = ZonedDateTimeMillisDeserializer::class)
    val createTime: ZonedDateTime = ZonedDateTime.now()
    var queuePosition = 0

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

        return true
    }

    override fun hashCode(): Int {
        var result = description?.hashCode() ?: 0
        result = 31 * result + (creator?.hashCode() ?: 0)
        result = 31 * result + createTime.hashCode()
        return result
    }

}
