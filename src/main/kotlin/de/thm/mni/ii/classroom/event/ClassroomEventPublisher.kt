package de.thm.mni.ii.classroom.event

import de.thm.mni.ii.classroom.model.classroom.DigitalClassroom
import de.thm.mni.ii.classroom.model.classroom.Ticket
import de.thm.mni.ii.classroom.model.classroom.User
import reactor.core.publisher.Mono

class ClassroomEventPublisher(
    private val digitalClassroom: DigitalClassroom
    ) {

    private fun createTicket(ticket: Ticket): Mono<Void> {
        val event = TicketEvent(ticket, true)
        return sendToAll(event)
    }

    private fun userConnected(user: User): Mono<Void> {
        return digitalClassroom.isUserInConference(user).map { isInConference ->
            UserEvent(user, joined = true, inConference = isInConference, conferenceId = null)
        }.flatMap(this::sendToAll)
    }

    private fun userDisconnected(user: User): Mono<Void> {
        val event = UserEvent(user, joined = false, inConference = false, conferenceId = null)
        return sendToAll(event)
    }

    private fun sendToAll(event: ClassroomEvent): Mono<Void> {
        return digitalClassroom.getSockets().doOnNext {
            it.next(event)
        }.then()
    }

}
