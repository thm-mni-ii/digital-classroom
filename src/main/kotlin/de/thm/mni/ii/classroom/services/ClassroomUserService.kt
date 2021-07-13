package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.event.TicketCreatedEvent
import de.thm.mni.ii.classroom.model.Ticket
import de.thm.mni.ii.classroom.model.User
import de.thm.mni.ii.classroom.security.classroom.ClassroomAuthentication
import de.thm.mni.ii.classroom.security.exception.UnauthorizedException
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.lang.IllegalArgumentException

@Service
class ClassroomUserService(private val classroomInstanceService: ClassroomInstanceService,
                           private val applicationEventPublisher: ApplicationEventPublisher) {

    fun openClassroom(user: User): Mono<String> = Mono.create {
        val classroom = classroomInstanceService.getClassroomInstance(user.classroomId)
        it.success("Hallo ${user.fullName}. Deine Rolle: ${user.userRole.name}\n" +
                " Meeting: ${classroom.classroomName}\n" +
                " ${classroom.classroomId}")
    }

    fun createTicket(auth: ClassroomAuthentication, ticket: Ticket): Flux<Ticket> {
        if (ticket.creator != auth.user) {
            throw IllegalArgumentException()
        }
        return classroomInstanceService
            .getClassroomInstance(auth.getClassroomId())
            .createTicket(ticket)
            .doOnComplete{ applicationEventPublisher.publishEvent(TicketCreatedEvent(ticket)) }
    }

    fun getTickets(auth: ClassroomAuthentication): Flux<Ticket> {
        return classroomInstanceService
            .getClassroomInstance(auth.getClassroomId())
            .getTickets()
    }



    fun assignTicket(auth: ClassroomAuthentication, ticket: Ticket): Flux<Ticket> {
        return classroomInstanceService
            .getClassroomInstance(auth.getClassroomId())
            .assignTicket(ticket)
    }

    fun getUsers(auth: ClassroomAuthentication): Flux<User> {
        return classroomInstanceService
            .getClassroomInstance(auth.getClassroomId())
            .getUsers()
    }

    fun deleteTicket(auth: ClassroomAuthentication, ticket: Ticket): Flux<Ticket> {
        if (auth.user!!.isPrivileged() || auth.user == ticket.creator) {
            return classroomInstanceService
                .getClassroomInstance(auth.getClassroomId())
                .deleteTicket(ticket)
        } else throw UnauthorizedException("User not authorized to delete ticket!")


    }

}