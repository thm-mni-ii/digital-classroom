package de.thm.mni.ii.classroom.exception.classroom

import de.thm.mni.ii.classroom.model.classroom.Ticket

class TicketAlreadyExistsException(ticket: Ticket) : ClassroomException("Similar ticket \"${ticket.description}\" already exists!")
