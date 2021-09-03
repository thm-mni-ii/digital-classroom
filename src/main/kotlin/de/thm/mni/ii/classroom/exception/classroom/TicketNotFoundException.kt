package de.thm.mni.ii.classroom.exception.classroom

import de.thm.mni.ii.classroom.model.classroom.Ticket

class TicketNotFoundException(ticket: Ticket):
    ClassroomException("Ticket ${ticket.classroomId}/${ticket.ticketId} not found!")
