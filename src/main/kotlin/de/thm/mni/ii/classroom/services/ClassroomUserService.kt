package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.event.TicketAction
import de.thm.mni.ii.classroom.event.TicketEvent
import de.thm.mni.ii.classroom.event.UserAction
import de.thm.mni.ii.classroom.event.UserEvent
import de.thm.mni.ii.classroom.model.classroom.ClassroomInfo
import de.thm.mni.ii.classroom.model.classroom.ConferenceInfo
import de.thm.mni.ii.classroom.model.classroom.Ticket
import de.thm.mni.ii.classroom.model.classroom.User
import de.thm.mni.ii.classroom.model.classroom.UserDisplay
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

    fun userConnected(user: User, socketRequester: RSocketRequester): Mono<Void> {
        return classroomInstanceService.getClassroomInstance(user.classroomId)
            .delayUntil { classroom -> this.conferenceService.updateConferences(classroom) }
            .zipWhen { classroom ->
                classroom.connectSocket(user, socketRequester)
            }.delayUntil { (classroom, userDisplay) ->
                classroom.sendToAll(UserEvent(userDisplay, UserAction.JOIN))
            }.doOnSuccess {
                logger.info("$user connected to ${user.classroomId}!")
            }.flatMap {
                socketRequester.rsocketClient().source()
            }.doOnNext {
                it.onClose().doOnSuccess {
                    userDisconnected(user)
                }.doOnError { exception ->
                    userDisconnected(user, exception)
                }.subscribe()
            }.thenEmpty(Mono.empty())
    }

    fun userDisconnected(user: User, throwable: Throwable? = null) {
        classroomInstanceService.getClassroomInstance(user.classroomId)
            .delayUntil { classroom ->
                classroom.disconnectSocket(user)
                conferenceService.removeUserFromAllConferences(classroom, user)
            }.delayUntil { classroom ->
                classroom.sendToAll(UserEvent(UserDisplay(user, true), userAction = UserAction.LEAVE))
            }.doOnNext {
                if (throwable == null) {
                    logger.info("$user disconnected from ${user.classroomId}!")
                } else {
                    logger.error("$user disconnected from ${user.classroomId} with error {}!", throwable.message)
                }
            }.subscribe()
    }

    fun getTickets(user: User): Flux<Ticket> {
        return classroomInstanceService
            .getClassroomInstance(user.classroomId)
            .flatMapMany { it.getTickets() }
    }

    fun createTicket(user: User, ticket: Ticket) {
        classroomInstanceService
            .getClassroomInstance(user.classroomId)
            .filter {
                ticket.creator == user && ticket.classroomId == user.classroomId
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

    fun assignTicket(user: User, receivedTicket: Ticket) {
        classroomInstanceService
            .getClassroomInstance(user.classroomId)
            .filter {
                user.isPrivileged() && receivedTicket.assignee!!.isPrivileged() && receivedTicket.classroomId == user.classroomId
            }.switchIfEmpty {
                Mono.error(UnauthorizedException("User not authorized to assign ticket!"))
            }.flatMap {
                it.assignTicket(receivedTicket, receivedTicket.assignee!!)
            }.delayUntil { (ticket, classroom) ->
                classroom.sendToAll(TicketEvent(ticket, TicketAction.ASSIGN))
            }.doOnSuccess { (ticket, classroom) ->
                logger.info("Ticket ${classroom.classroomName} / ${ticket.ticketId} assigned to ${ticket.assignee!!.fullName}!")
            }.subscribe()
    }

    fun closeTicket(user: User, ticket: Ticket) {
        classroomInstanceService
            .getClassroomInstance(user.classroomId)
            .filter {
                ticket.classroomId == user.classroomId &&
                    (user.isPrivileged() || ticket.creator == user)
            }.switchIfEmpty {
                Mono.error(UnauthorizedException("User not authorized to delete ticket!"))
            }.flatMap { classroom ->
                classroom.deleteTicket(ticket)
            }.delayUntil { (ticket, classroom) ->
                classroom.sendToAll(TicketEvent(ticket, TicketAction.CLOSE))
            }.doOnSuccess { (ticket, classroom) ->
                logger.info("Ticket ${classroom.classroomName} / ${ticket.ticketId} assigned to ${ticket.assignee?.fullName ?: "N/A"}!")
            }.subscribe()
    }

    fun getUsers(user: User): Flux<User> {
        return classroomInstanceService
            .getClassroomInstance(user.classroomId)
            .flatMapMany { it.getUsersFlux() }
    }

    fun getUserDisplays(user: User): Flux<UserDisplay> {
        return classroomInstanceService
            .getClassroomInstance(user.classroomId)
            .flatMapMany { it.getUsersFlux() }
    }

    fun getClassroomInfo(user: User): Mono<ClassroomInfo> {
        return classroomInstanceService
            .getClassroomInstance(user.classroomId)
            .cast(ClassroomInfo::class.java)
    }

    fun getConferences(user: User): Flux<ConferenceInfo> {
        return classroomInstanceService
            .getClassroomInstance(user.classroomId)
            .flatMapMany { classroom ->
                classroom.conferences.getConferences()
            }.map(::ConferenceInfo)
    }

    fun changeVisibility(user: User, event: UserEvent) {
        assert(user == event.user)
        classroomInstanceService
            .getClassroomInstance(user.classroomId)
            .flatMap { classroom ->
                Mono.zip(Mono.just(classroom), classroom.changeVisibility(event.user))
            }.flatMap { (classroom, user) ->
                classroom.sendToAll(UserEvent(user, UserAction.VISIBILITY_CHANGE))
            }.subscribe()
    }
}
