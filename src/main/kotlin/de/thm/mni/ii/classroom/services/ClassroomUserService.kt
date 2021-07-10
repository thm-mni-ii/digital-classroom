package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.model.Ticket
import de.thm.mni.ii.classroom.model.User
import de.thm.mni.ii.classroom.security.classroom.ClassroomAuthentication
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.lang.IllegalArgumentException

@Service
class ClassroomUserService(private val classroomInstanceService: ClassroomInstanceService) {

    fun openClassroom(user: User): Mono<String> = Mono.create {
        val classroom = classroomInstanceService.getClassroomInstance(user.classroomId)
        it.success("Hallo ${user.fullName}. Deine Rolle: ${user.userRole.name}\n" +
                " Meeting: ${classroom.classroomName}\n" +
                " ${classroom.meetingID}")
    }

    fun createTicket(auth: ClassroomAuthentication, ticket: Ticket): Flux<Ticket> {
        if (ticket.creator != auth.user) {
            throw IllegalArgumentException()
        }
        return classroomInstanceService
            .getClassroomInstance(auth.getClassroomId())
            .createTicket(ticket)
    }

    fun getTickets(auth: ClassroomAuthentication): Flux<Ticket> {
        return classroomInstanceService
            .getClassroomInstance(auth.getClassroomId())
            .getTickets()
    }

}