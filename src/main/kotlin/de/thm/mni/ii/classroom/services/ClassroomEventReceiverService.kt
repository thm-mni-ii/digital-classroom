package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.event.*
import de.thm.mni.ii.classroom.model.classroom.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class ClassroomEventReceiverService(
    private val userService: ClassroomUserService,
    private val conferenceService: ConferenceService) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun classroomEventReceived(user: User, event: ClassroomEvent) {
        when (event) {
            is MessageEvent -> messageEventReceived(user, event)
            is TicketEvent -> ticketEventReceived(user, event)
            is ConferenceEvent -> conferenceEventReceived(user, event)
            else -> {
                logger.info("Received unknown event! ${event.javaClass.name}")
            }
        }
    }

    private fun messageEventReceived(user: User, messageEvent: MessageEvent): Mono<Void> {
        logger.info("Received message ${messageEvent.message} from ${user.fullName}")
        return Mono.empty()
    }

    private fun ticketEventReceived(user: User, ticketEvent: TicketEvent) {
        when (ticketEvent.ticketAction) {
            TicketAction.CREATE -> userService.createTicket(user, ticketEvent.ticket)
            TicketAction.ASSIGN -> userService.assignTicket(user, ticketEvent.ticket)
            TicketAction.CLOSE  -> userService.closeTicket(user, ticketEvent.ticket)
        }
    }

    private fun conferenceEventReceived(user: User, conferenceEvent: ConferenceEvent) {
        when (conferenceEvent.conferenceAction) {
            ConferenceAction.CREATE -> conferenceService.createConference(user, conferenceEvent.conferenceInfo)
            ConferenceAction.END -> conferenceService.endConference(user, conferenceEvent.conferenceInfo)
            ConferenceAction.HIDE -> conferenceService.hideConference(user, conferenceEvent.conferenceInfo)
            ConferenceAction.PUBLISH -> conferenceService.publishConference(user, conferenceEvent.conferenceInfo)
        }
    }

}
