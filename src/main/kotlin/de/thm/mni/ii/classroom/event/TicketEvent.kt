package de.thm.mni.ii.classroom.event

import com.fasterxml.jackson.annotation.JsonIgnore
import de.thm.mni.ii.classroom.model.classroom.Ticket

data class TicketEvent(
    val ticket: Ticket,
    val ticketAction: TicketAction,
) : ClassroomEvent(TicketEvent::class.simpleName!!) {
    @JsonIgnore override fun getClassroomId(): String = this.ticket.classroomId
}

enum class TicketAction {
    CREATE,
    ASSIGN,
    CLOSE,
    EDIT
}
