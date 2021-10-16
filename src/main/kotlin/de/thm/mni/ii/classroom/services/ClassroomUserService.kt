package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.event.TicketAction
import de.thm.mni.ii.classroom.event.TicketEvent
import de.thm.mni.ii.classroom.event.UserAction
import de.thm.mni.ii.classroom.event.UserEvent
import de.thm.mni.ii.classroom.model.classroom.ClassroomInfo
import de.thm.mni.ii.classroom.model.classroom.ConferenceInfo
import de.thm.mni.ii.classroom.model.classroom.DigitalClassroom
import de.thm.mni.ii.classroom.model.classroom.Ticket
import de.thm.mni.ii.classroom.model.classroom.User
import de.thm.mni.ii.classroom.model.classroom.UserCredentials
import de.thm.mni.ii.classroom.security.exception.UnauthorizedException
import de.thm.mni.ii.classroom.util.component1
import de.thm.mni.ii.classroom.util.component2
import org.slf4j.LoggerFactory
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.lang.IllegalArgumentException

@Service
class ClassroomUserService(
    private val classroomInstanceService: ClassroomInstanceService,
    private val conferenceService: ConferenceService
) {

    private val logger = LoggerFactory.getLogger(ClassroomUserService::class.java)

    fun userConnected(userCredentials: UserCredentials, socketRequester: RSocketRequester): Mono<Void> {
        return classroomInstanceService.getClassroomInstance(userCredentials.classroomId)
            .doOnNext { classroom -> this.conferenceService.updateConferences(classroom).subscribe() }
            .zipWhen { classroom ->
                classroom.connectSocket(userCredentials, socketRequester)
            }.delayUntil { (classroom, userDisplay) ->
                classroom.sendToAll(UserEvent(userDisplay, UserAction.JOIN))
            }.doOnSuccess {
                logger.info("$userCredentials connected to ${userCredentials.classroomId}!")
            }.map {
                socketRequester.rsocket()!!
            }.doOnNext { socket ->
                socket.onClose().doOnSuccess {
                    userDisconnected(userCredentials)
                }.doOnError { exception ->
                    userDisconnected(userCredentials, exception)
                }.subscribe()
            }.then()
    }

    fun userDisconnected(userCredentials: UserCredentials, throwable: Throwable? = null) {
        classroomInstanceService.getClassroomInstance(userCredentials.classroomId)
            .zipWhen { classroom ->
                classroom.disconnectSocket(userCredentials)
            }.delayUntil { (classroom, user) ->
                classroom.sendToAll(UserEvent(user, userAction = UserAction.LEAVE))
            }.doOnNext {
                if (throwable == null) {
                    logger.info("$userCredentials disconnected from ${userCredentials.classroomId}!")
                } else {
                    logger.error("$userCredentials disconnected from ${userCredentials.classroomId} with error {}!", throwable.message)
                }
            }.subscribe()
    }

    fun getTickets(userCredentials: UserCredentials): Flux<Ticket> {
        return classroomInstanceService
            .getClassroomInstance(userCredentials.classroomId)
            .flatMapMany { it.getTickets() }
    }

    fun createTicket(userCredentials: UserCredentials, ticket: Ticket) {
        classroomInstanceService
            .getClassroomInstance(userCredentials.classroomId)
            .filter {
                ticket.creator == userCredentials && ticket.classroomId == userCredentials.classroomId
            }.switchIfEmpty {
                Mono.error(IllegalArgumentException())
            }.flatMap {
                it.createTicket(ticket)
            }.delayUntil { (ticket, classroom) ->
                classroom.sendToAll(TicketEvent(ticket, TicketAction.CREATE))
            }.doOnSuccess { (ticket, classroom) ->
                logger.info("Ticket ${classroom.classroomName} / ${ticket.ticketId} created!")
            }.subscribe()
    }

    fun assignTicket(userCredentials: UserCredentials, receivedTicket: Ticket) {
        classroomInstanceService
            .getClassroomInstance(userCredentials.classroomId)
            .filter {
                userCredentials.isPrivileged() && receivedTicket.assignee!!.isPrivileged()
            }.switchIfEmpty {
                Mono.error(UnauthorizedException("User not authorized to assign ticket!"))
            }.flatMap {
                it.assignTicket(receivedTicket, receivedTicket.assignee!!)
            }.delayUntil { (ticket, classroom) ->
                classroom.sendToAll(TicketEvent(ticket, TicketAction.UPDATE))
            }.doOnSuccess { (ticket, classroom) ->
                logger.info("Ticket ${classroom.classroomName} / ${ticket.ticketId} assigned to ${ticket.assignee!!.fullName}!")
            }.subscribe()
    }

    fun closeTicket(userCredentials: UserCredentials, ticket: Ticket) {
        classroomInstanceService
            .getClassroomInstance(userCredentials.classroomId)
            .filter {
                ticket.classroomId == userCredentials.classroomId &&
                    (userCredentials.isPrivileged() || ticket.creator == userCredentials)
            }.switchIfEmpty {
                Mono.error(UnauthorizedException("User not authorized to delete ticket!"))
            }.delayUntil { classroom ->
                classroom.deleteTicket(ticket)
            }.delayUntil { classroom ->
                classroom.sendToAll(TicketEvent(ticket, TicketAction.CLOSE))
            }.flatMap { classroom ->
                logger.info("Ticket ${classroom.classroomName} / ${ticket.ticketId} assigned to ${ticket.assignee?.fullName ?: "N/A"}!")
                closeConferenceOfClosedTicket(ticket, classroom)
            }.subscribe()
    }

    private fun closeConferenceOfClosedTicket(ticket: Ticket, classroom: DigitalClassroom): Mono<Void> {
        return classroom.conferences.getConferenceOfTicket(ticket.ticketId)
            .filter { conference ->
                conference.attendees.isEmpty()
            }.doOnNext { conference ->
                this.conferenceService.scheduleConferenceDeletion(classroom, conference, 10)
            }.then()
    }

    fun getUsers(userCredentials: UserCredentials): Flux<UserCredentials> {
        return classroomInstanceService
            .getClassroomInstance(userCredentials.classroomId)
            .flatMapMany { it.getUsersFlux() }
    }

    fun getUserDisplays(userCredentials: UserCredentials): Flux<User> {
        return classroomInstanceService
            .getClassroomInstance(userCredentials.classroomId)
            .flatMapMany { it.getUsersFlux() }
    }

    fun getClassroomInfo(userCredentials: UserCredentials): Mono<ClassroomInfo> {
        return classroomInstanceService
            .getClassroomInstance(userCredentials.classroomId)
            .cast(ClassroomInfo::class.java)
    }

    fun getConferences(userCredentials: UserCredentials): Flux<ConferenceInfo> {
        return classroomInstanceService
            .getClassroomInstance(userCredentials.classroomId)
            .flatMapMany { classroom ->
                classroom.conferences.getConferences()
            }.map(::ConferenceInfo)
    }

    fun changeVisibility(userCredentials: UserCredentials, event: UserEvent) {
        assert(userCredentials == event.user)
        classroomInstanceService
            .getClassroomInstance(userCredentials.classroomId)
            .zipWhen { classroom ->
                classroom.changeVisibility(event.user)
            }.flatMap { (classroom, user) ->
                classroom.sendToAll(UserEvent(user, UserAction.VISIBILITY_CHANGE))
            }.subscribe()
    }
}
