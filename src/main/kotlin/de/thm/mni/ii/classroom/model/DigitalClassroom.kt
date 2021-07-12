package de.thm.mni.ii.classroom.model

import de.thm.mni.ii.classroom.security.exception.InvalidMeetingPasswordException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashSet

/**
 * Class representing a digital classroom instance.
 */
data class DigitalClassroom(
    val meetingID: String,
    val studentPassword: String,
    val tutorPassword: String,
    val teacherPassword: String,
    val classroomName: String,
    val internalClassroomId: String
) {

    private val users = HashSet<User>()
    private val tickets = LinkedHashSet<Ticket>()

    val creationTimestamp: ZonedDateTime = ZonedDateTime.now()

    fun hasUserJoined() = users.isNotEmpty()
    fun hasBeenForciblyEnded() = false
    fun getDuration() = ChronoUnit.MINUTES.between(creationTimestamp, ZonedDateTime.now())

    fun doesUserExist(user: User): Boolean = users.contains(user)

    fun joinUser(password: String, user: User): User {
        when (password) {
            studentPassword -> user.userRole = UserRole.STUDENT
            teacherPassword -> user.userRole = UserRole.TEACHER
            tutorPassword -> user.userRole = UserRole.TUTOR
            else -> throw InvalidMeetingPasswordException(meetingID)
        }
        users.add(user)
        return user
    }

    fun createTicket(ticket: Ticket): Flux<Ticket> {
        tickets.add(ticket)
        return tickets.mapIndexed(this::applyIndex).toFlux()
    }

    fun getTickets(): Flux<Ticket> {
        return tickets.mapIndexed(this::applyIndex).toFlux()
    }

    fun getUsers(): Flux<User> {
        return users.toFlux()
    }

    fun assignTicket(ticket: Ticket): Flux<Ticket> {
        // This works, because tickets are equal when their creator, title and description are equal.
        tickets.remove(ticket)
        tickets.add(ticket)
        return tickets.mapIndexed(this::applyIndex).toFlux()
    }

    fun deleteTicket(ticket: Ticket): Flux<Ticket> {
        tickets.remove(ticket)
        return tickets.mapIndexed(this::applyIndex).toFlux()
    }

    private fun applyIndex(index: Int, ticket: Ticket): Ticket {
        ticket.queuePosition = index
        return ticket
    }


}
