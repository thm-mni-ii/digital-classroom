package de.thm.mni.ii.classroom.model

import java.time.LocalDateTime

class Ticket(val title: String,
                  val description: String,
                  val creator: User?,
                  val assignee: User?,
                  val createTime: LocalDateTime = LocalDateTime.now()
): Comparable<Ticket> {
    /**
     * No-arg constructor for Jackson serialization
     */
    constructor(): this("", "", null, null)

    override fun compareTo(other: Ticket): Int {
        return this.createTime.compareTo(other.createTime)
    }
}
