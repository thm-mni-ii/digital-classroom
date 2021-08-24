package de.thm.mni.ii.classroom.model.classroom

import de.thm.mni.ii.classroom.event.ClassroomEvent
import de.thm.mni.ii.classroom.exception.classroom.TicketAlreadyExistsException
import de.thm.mni.ii.classroom.security.exception.InvalidMeetingPasswordException
import de.thm.mni.ii.classroom.util.update
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

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

    private val users = HashMap<User, FluxSink<ClassroomEvent>?>()
    private val tickets = HashSet<Ticket>()
    private val conferenceStorage = ConferenceStorage(this)

    val creationTimestamp: ZonedDateTime = ZonedDateTime.now()

    fun hasUserJoined() = users.filterValues { it != null }.isNotEmpty()
    fun hasBeenForciblyEnded() = false
    fun getDuration() = ChronoUnit.MINUTES.between(creationTimestamp, ZonedDateTime.now())

    fun doesUserExist(user: User): Boolean = users.contains(user)

    fun joinUser(password: String, user: User): Mono<User> {
        return Mono.defer {
            when (password) {
                studentPassword -> user.userRole = UserRole.STUDENT
                teacherPassword -> user.userRole = UserRole.TEACHER
                tutorPassword -> user.userRole = UserRole.TUTOR
                else -> throw InvalidMeetingPasswordException(classroomId)
            }
            users[user] = null
            Mono.just(user)
        }
    }

    fun connectSocket(user: User, socket: FluxSink<ClassroomEvent>) {
        users[user] = socket
    }

    fun createTicket(ticket: Ticket): Mono<Ticket> {
        return if (tickets.add(ticket)) {
            Mono.just(ticket)
        } else {
            Mono.error(TicketAlreadyExistsException(ticket))
        }
    }

    fun getTickets(): Flux<Ticket> {
        return tickets.toFlux()
    }

    fun getUsers(): Flux<User> {
        return users.keys.toFlux()
    }

    fun assignTicket(ticket: Ticket, user: User): Mono<Ticket> {
        return Mono.justOrEmpty(tickets.find { it == ticket }?.apply { assignee = user })
    }

    fun deleteTicket(ticket: Ticket): Mono<Boolean> {
        return Mono.just(tickets.remove(ticket))
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

    fun getSockets(): Flux<FluxSink<ClassroomEvent>> = Flux.fromIterable(users.values)

    fun getSocketOfUser(user: User): Mono<FluxSink<ClassroomEvent>> = Mono.justOrEmpty(users[user])

    fun isUserInConference(user: User): Mono<Boolean> {
        return conferenceStorage.isUserInConference(user)
    }


}
