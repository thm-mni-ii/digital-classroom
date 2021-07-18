package de.thm.mni.ii.classroom.model

import com.google.common.collect.HashBiMap
import de.thm.mni.ii.classroom.security.exception.InvalidMeetingPasswordException
import de.thm.mni.ii.classroom.util.update
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashSet

/**
 * Class representing a digital classroom instance.
 */
data class DigitalClassroom(
    val classroomId: String,
    val studentPassword: String,
    val tutorPassword: String,
    val teacherPassword: String,
    val classroomName: String,
    val internalClassroomId: String // BBB API Specification
) {

    private val users = HashSet<User>()
    private val tickets = LinkedHashSet<Ticket>()
    private val conferenceStorage = ConferenceStorage(this)

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
            else -> throw InvalidMeetingPasswordException(classroomId)
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
        tickets.update(ticket)
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

    fun getConferenceOfUser(user: User): Mono<Conference> {
        return conferenceStorage.getConferenceOfUser(user)
    }

    fun getConferences(): Flux<Conference> {
        return conferenceStorage.getConferences()
    }

    fun saveConference(conference: Conference): Mono<Conference> {
        return conferenceStorage.createConference(conference)
    }

    fun joinUserToConference(conference: Conference, user: User): Flux<User> {
        return conferenceStorage.joinUser(conference, user)
    }

    fun getUsersInConferences(): Flux<User> {
        return conferenceStorage.getUsersInConferences()
    }


}
