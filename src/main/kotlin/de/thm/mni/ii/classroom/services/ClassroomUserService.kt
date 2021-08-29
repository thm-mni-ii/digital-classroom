package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.event.TicketAction
import de.thm.mni.ii.classroom.event.TicketEvent
import de.thm.mni.ii.classroom.event.UserAction
import de.thm.mni.ii.classroom.event.UserEvent
import de.thm.mni.ii.classroom.model.classroom.ClassroomInfo
import de.thm.mni.ii.classroom.model.classroom.DigitalClassroom
import de.thm.mni.ii.classroom.model.classroom.Ticket
import de.thm.mni.ii.classroom.model.classroom.User
import de.thm.mni.ii.classroom.security.exception.UnauthorizedException
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
    private val senderService: ClassroomEventSenderService
    ) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun userConnected(user: User, socketRequester: RSocketRequester): Mono<Void> {
        return classroomInstanceService.getClassroomInstance(user.classroomId)
            .doOnNext { classroom ->
                classroom.connectSocket(user, socketRequester)
            }.doOnNext { classroom ->
                senderService.sendToAll(classroom, UserEvent(user, userAction = UserAction.JOIN)).subscribe()
            }.doOnSuccess {
                logger.info("${user.userId}/${user.fullName} connected to ${user.classroomId}!")
            }.flatMap {
                socketRequester.rsocketClient().source()}
            .doOnNext {
                it.onClose().doOnSuccess {
                    userDisconnected(user)
                }.doOnError {
                    userDisconnected(user, it)
                }.subscribe()
            }.thenEmpty(Mono.empty())
    }

    private fun userDisconnected(user: User, throwable: Throwable? = null) {
        classroomInstanceService.getClassroomInstance(user.classroomId)
            .doOnNext { classroom ->
                classroom.disconnectSocket(user)
            }.doOnNext { classroom ->
                senderService.sendToAll(classroom, UserEvent(user, userAction = UserAction.LEAVE)).subscribe()
            }.doOnNext {
                if (throwable == null) {
                    logger.info("${user.userId} / ${user.fullName} disconnected from ${user.classroomId}!")
                } else {
                    logger.error("${user.userId} / ${user.fullName} disconnected from ${user.classroomId} with error {}!", throwable.message)
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
            }.doOnNext { (ticket, classroom) ->
                senderService.sendToAll(classroom, TicketEvent(ticket, TicketAction.CREATE)).subscribe()
            }.doOnSuccess { (ticket, classroom) ->
                logger.info("Ticket ${classroom.classroomName}/${ticket.ticketId} created!")
            }.subscribe()
    }

    fun assignTicket(user: User, ticket: Ticket) {
        classroomInstanceService
            .getClassroomInstance(user.classroomId)
            .filter {
                user.isPrivileged() && ticket.classroomId == user.classroomId
            }.switchIfEmpty {
                Mono.error(IllegalArgumentException())
            }.flatMap {
                it.assignTicket(ticket, user)
            }.doOnNext { (ticket, classroom) ->
                senderService.sendToAll(classroom, TicketEvent(ticket, TicketAction.ASSIGN)).subscribe()
            }.doOnSuccess { (ticket, classroom) ->
                logger.info("Ticket ${classroom.classroomName}/${ticket.ticketId} assigned to ${ticket.assignee!!.fullName}!")
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
            }.flatMap {
                it.deleteTicket(ticket)
            }.doOnNext { (ticket, classroom) ->
                senderService.sendToAll(classroom, TicketEvent(ticket, TicketAction.CLOSE)).subscribe()
            }.doOnSuccess { (ticket, classroom) ->
                logger.info("Ticket ${classroom.classroomName}/${ticket.ticketId} assigned to ${ticket.assignee!!.fullName}!")
            }.subscribe()
    }

    fun getUsers(user: User): Flux<User> {
        return classroomInstanceService
            .getClassroomInstance(user.classroomId)
            .flatMapMany { it.getUsers() }
    }

    fun getClassroomInfo(user: User): Mono<ClassroomInfo> {
        return classroomInstanceService
            .getClassroomInstance(user.classroomId)
            .map { ClassroomInfo(it.classroomId, it.classroomName) }
    }

}
