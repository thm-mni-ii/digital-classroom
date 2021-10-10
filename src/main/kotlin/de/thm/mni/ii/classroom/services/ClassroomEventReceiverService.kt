package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.event.ClassroomEvent
import de.thm.mni.ii.classroom.event.ConferenceAction
import de.thm.mni.ii.classroom.event.ConferenceEvent
import de.thm.mni.ii.classroom.event.MessageEvent
import de.thm.mni.ii.classroom.event.TicketAction
import de.thm.mni.ii.classroom.event.TicketEvent
import de.thm.mni.ii.classroom.event.UserAction
import de.thm.mni.ii.classroom.event.UserEvent
import de.thm.mni.ii.classroom.model.classroom.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class ClassroomEventReceiverService(
    private val userService: ClassroomUserService,
    private val conferenceService: ConferenceService
) {

    private val logger: Logger = LoggerFactory.getLogger(ClassroomEventReceiverService::class.java)

    fun classroomEventReceived(user: User, event: ClassroomEvent) {
        when (event) {
            is MessageEvent -> messageEventReceived(user, event)
            is TicketEvent -> ticketEventReceived(user, event)
            is ConferenceEvent -> conferenceEventReceived(user, event)
            is UserEvent -> userEventReceived(user, event)
            else -> {
                logger.info("Received unknown event! ${event.javaClass.name}")
            }
        }
    }

    private fun userEventReceived(user: User, event: UserEvent) {
        when (event.userAction) {
            UserAction.JOIN -> {}
            UserAction.LEAVE -> {}
            UserAction.VISIBILITY_CHANGE -> userService.changeVisibility(user, event)
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
            TicketAction.CLOSE -> userService.closeTicket(user, ticketEvent.ticket)
        }
    }

    private fun conferenceEventReceived(user: User, conferenceEvent: ConferenceEvent) {
        when (conferenceEvent.conferenceAction) {
            ConferenceAction.CREATE -> conferenceService.createConference(user, conferenceEvent.conferenceInfo)
            ConferenceAction.CLOSE -> conferenceService.endConference(user, conferenceEvent.conferenceInfo)
            ConferenceAction.VISIBILITY -> conferenceService.changeVisibility(user, conferenceEvent.conferenceInfo)
            ConferenceAction.USER_CHANGE -> logger.error("Received USER_CHANGE event from ${user.fullName}! This should never happen")
        }
    }
}
