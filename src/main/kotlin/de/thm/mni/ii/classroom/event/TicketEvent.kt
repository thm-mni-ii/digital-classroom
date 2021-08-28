package de.thm.mni.ii.classroom.event

import de.thm.mni.ii.classroom.model.classroom.Ticket

data class TicketEvent(
    val ticket: Ticket,
    val ticketAction: TicketAction,
    ): ClassroomEvent(TicketEvent::class.simpleName!!)

enum class TicketAction {
    CREATE,
    ASSIGN,
    CLOSE
}
