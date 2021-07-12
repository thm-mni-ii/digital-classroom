package de.thm.mni.ii.classroom.model

import java.time.ZonedDateTime

data class Ticket(
    val description: String?,
    val creator: User?,
    val assignee: User?,
    val createTime: ZonedDateTime = ZonedDateTime.now()
): Comparable<Ticket> {

    var queuePosition = 0

    /**
     * No-arg constructor for Jackson serialization
     */
    constructor(): this("", null, null)

    override fun compareTo(other: Ticket): Int {
        return this.createTime.compareTo(other.createTime)
    }

}
