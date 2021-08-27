package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.event.TicketEvent
import de.thm.mni.ii.classroom.model.classroom.Ticket
import de.thm.mni.ii.classroom.model.classroom.User
import de.thm.mni.ii.classroom.security.jwt.ClassroomAuthentication
import de.thm.mni.ii.classroom.security.exception.UnauthorizedException
import de.thm.mni.ii.classroom.util.logThread
import org.slf4j.LoggerFactory
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.lang.IllegalArgumentException

@Service
class ClassroomUserSocketService(private val classroomInstanceService: ClassroomInstanceService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun userConnected(user: User, socketRequester: RSocketRequester) {
        classroomInstanceService.getClassroomInstance(user.classroomId).subscribe { classroom ->
            classroom.connectSocket(user, socketRequester)
        }.dispose()
    }

    fun userDisconnected(user: User) {
        classroomInstanceService.getClassroomInstance(user.classroomId).subscribe { classroom ->
            classroom.disconnectSocket(user)
        }.dispose()
    }

    fun createTicket(user: User, ticket: Ticket): Mono<Ticket> {
        return classroomInstanceService
            .getClassroomInstance(user.classroomId)
            .filter {
                ticket.creator != user || ticket.classroomId != user.classroomId
            }.switchIfEmpty {
                Mono.error(IllegalArgumentException())
            }.flatMap {
                it.createTicket(ticket)
            }.doOnSuccess {
                logger.info("Ticket ${ticket.description} created!")
            }
    }

    fun getTickets(user: User): Flux<Ticket> {
        return classroomInstanceService
            .getClassroomInstance(user.classroomId)
            .flatMapMany { it.getTickets() }
    }

    fun assignTicket(user: User, ticket: Ticket): Mono<Ticket> {
        return classroomInstanceService
            .getClassroomInstance(user.classroomId)
            .flatMap {
                it.assignTicket(ticket, user)
            }.doOnSuccess {
                logger.info("Ticket ${ticket.description} assigned to ${ticket.assignee!!.fullName}!")
            }
    }

    fun getUsers(user: User): Flux<User> {
        return classroomInstanceService
            .getClassroomInstance(user.classroomId)
            .flatMapMany { it.getUsers() }
    }

    fun deleteTicket(user: User, ticket: Ticket): Mono<Boolean> {
        if (user!!.isPrivileged() || user == ticket.creator) {
            return classroomInstanceService
                .getClassroomInstance(user.classroomId)
                .flatMap { it.deleteTicket(ticket) }
        } else throw UnauthorizedException("User not authorized to delete ticket!")
    }

}
