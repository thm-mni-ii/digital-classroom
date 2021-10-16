package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.event.ClassroomEvent
import de.thm.mni.ii.classroom.event.ConferenceAction
import de.thm.mni.ii.classroom.event.ConferenceEvent
import de.thm.mni.ii.classroom.event.TicketAction
import de.thm.mni.ii.classroom.event.TicketEvent
import de.thm.mni.ii.classroom.event.UserAction
import de.thm.mni.ii.classroom.event.UserEvent
import de.thm.mni.ii.classroom.model.classroom.UserCredentials
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ClassroomEventReceiverService(
    private val userService: ClassroomUserService,
    private val conferenceService: ConferenceService
) {

    private val logger: Logger = LoggerFactory.getLogger(ClassroomEventReceiverService::class.java)

    fun classroomEventReceived(userCredentials: UserCredentials, event: ClassroomEvent) {
        when (event) {
            is TicketEvent -> ticketEventReceived(userCredentials, event)
            is ConferenceEvent -> conferenceEventReceived(userCredentials, event)
            is UserEvent -> userEventReceived(userCredentials, event)
            else -> {
                logger.info("Received unknown event! ${event.javaClass.name}")
            }
        }
    }

    private fun userEventReceived(userCredentials: UserCredentials, event: UserEvent) {
        when (event.userAction) {
            UserAction.JOIN -> {}
            UserAction.LEAVE -> {}
            UserAction.VISIBILITY_CHANGE -> userService.changeVisibility(userCredentials, event)
        }
    }

    private fun ticketEventReceived(userCredentials: UserCredentials, ticketEvent: TicketEvent) {
        when (ticketEvent.ticketAction) {
            TicketAction.CREATE -> userService.createTicket(userCredentials, ticketEvent.ticket)
            TicketAction.UPDATE -> userService.updateTicket(userCredentials, ticketEvent.ticket)
            TicketAction.CLOSE -> userService.closeTicket(userCredentials, ticketEvent.ticket)
        }
    }

    private fun conferenceEventReceived(userCredentials: UserCredentials, conferenceEvent: ConferenceEvent) {
        when (conferenceEvent.conferenceAction) {
            ConferenceAction.CREATE -> logger.error("Received CREATE event from ${userCredentials.fullName}! This should never happen")
            ConferenceAction.CLOSE -> conferenceService.endConference(userCredentials, conferenceEvent.conferenceInfo)
            ConferenceAction.VISIBILITY -> conferenceService.changeVisibility(userCredentials, conferenceEvent.conferenceInfo)
            ConferenceAction.USER_CHANGE -> logger.error("Received USER_CHANGE event from ${userCredentials.fullName}! This should never happen")
        }
    }
}
