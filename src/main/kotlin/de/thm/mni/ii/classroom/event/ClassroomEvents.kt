package de.thm.mni.ii.classroom.event

import de.thm.mni.ii.classroom.model.classroom.Ticket
import de.thm.mni.ii.classroom.model.classroom.User
import org.springframework.context.ApplicationEvent

abstract class ClassroomEvent(source: Any): ApplicationEvent(source)

class UserJoinedClassroomEvent(val user: User): ClassroomEvent(user)

class UserLeftClassroomEvent(val user: User): ClassroomEvent(user)

class TicketCreatedEvent(val ticket: Ticket): ClassroomEvent(ticket)

class TicketClosedEvent(val ticket: Ticket): ClassroomEvent(ticket)

class TicketChangedEvent(val ticket: Ticket): ClassroomEvent(ticket)