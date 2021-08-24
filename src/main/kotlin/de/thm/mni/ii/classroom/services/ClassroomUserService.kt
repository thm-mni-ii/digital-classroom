package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.model.classroom.Ticket
import de.thm.mni.ii.classroom.model.classroom.User
import de.thm.mni.ii.classroom.security.classroom.ClassroomAuthentication
import de.thm.mni.ii.classroom.security.exception.UnauthorizedException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.lang.IllegalArgumentException

@Service
class ClassroomUserService(private val classroomInstanceService: ClassroomInstanceService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun createTicket(auth: ClassroomAuthentication, ticket: Ticket): Mono<Ticket> {
        if (ticket.creator != auth.user) {
            throw IllegalArgumentException()
        }
        return classroomInstanceService
            .getClassroomInstance(auth.getClassroomId())
            .flatMap { it.createTicket(ticket) }
            .doOnSuccess{
                logger.info("Ticket ${ticket.description} created!")
            }
    }

    fun getTickets(auth: ClassroomAuthentication): Flux<Ticket> {
        return classroomInstanceService
            .getClassroomInstance(auth.getClassroomId())
            .flatMapMany { it.getTickets() }
    }



    fun assignTicket(auth: ClassroomAuthentication, ticket: Ticket): Mono<Ticket> {
        return classroomInstanceService
            .getClassroomInstance(auth.getClassroomId())
            .flatMap {
                it.assignTicket(ticket, auth.user!!)
            }.doOnSuccess {
                logger.info("Ticket ${ticket.description} assigned to ${ticket.assignee!!.fullName}!")
            }
    }

    fun getUsers(auth: ClassroomAuthentication): Flux<User> {
        return classroomInstanceService
            .getClassroomInstance(auth.getClassroomId())
            .flatMapMany { it.getUsers() }
    }

    fun deleteTicket(auth: ClassroomAuthentication, ticket: Ticket): Mono<Boolean> {
        if (auth.user!!.isPrivileged() || auth.user == ticket.creator) {
            return classroomInstanceService
                .getClassroomInstance(auth.getClassroomId())
                .flatMap { it.deleteTicket(ticket) }
        } else throw UnauthorizedException("User not authorized to delete ticket!")


    }

}
