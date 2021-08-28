package de.thm.mni.ii.classroom.model.classroom

import de.thm.mni.ii.classroom.exception.classroom.TicketAlreadyExistsException
import de.thm.mni.ii.classroom.exception.classroom.TicketNotFoundException
import de.thm.mni.ii.classroom.security.exception.InvalidMeetingPasswordException
import org.springframework.messaging.rsocket.RSocketRequester
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.atomic.AtomicLong

/**
 * Class representing a digital classroom instance.
 */
class DigitalClassroom(
    classroomId: String,
    val studentPassword: String,
    val tutorPassword: String,
    val teacherPassword: String,
    classroomName: String
): ClassroomInfo(classroomId, classroomName) {

    private val users = HashMap<User, RSocketRequester?>()
    private val tickets = HashSet<Ticket>()
    private val nextTicketId = AtomicLong(10000L)
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

    fun connectSocket(user: User, socketRequester: RSocketRequester) {
        users[user] = socketRequester
    }

    fun disconnectSocket(user: User) {
        users.remove(user)
    }

    fun getTickets(): Flux<Ticket> {
        return tickets.toFlux()
    }

    fun createTicket(ticket: Ticket): Mono<Pair<Ticket, DigitalClassroom>> {
        return Mono.just(ticket).apply {
            ticket.ticketId = nextTicketId.getAndIncrement()
        }.flatMap { newTicket ->
            if (tickets.add(newTicket)) {
                Mono.just(newTicket)
            } else {
                Mono.error(TicketAlreadyExistsException(newTicket))
            }
        }.map { Pair(it, this) }
    }

    fun assignTicket(ticket: Ticket, user: User): Mono<Pair<Ticket, DigitalClassroom>> {
        return Mono.justOrEmpty(tickets.find { it == ticket })
            .switchIfEmpty(Mono.error(TicketNotFoundException(ticket)))
            .map { it.apply { assignee = user } }
            .map { Pair(it, this) }
    }

    fun deleteTicket(ticket: Ticket): Mono<Pair<Ticket, DigitalClassroom>> {
        return Mono.justOrEmpty(tickets.find { it == ticket })
            .switchIfEmpty(Mono.error(TicketNotFoundException(ticket)))
            .doOnNext { tickets.remove(it) }
            .map { Pair(it, this) }
    }

    fun getUsers(): Flux<User> {
        return users.keys.toFlux()
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

    fun getSockets(): Flux<Pair<User, RSocketRequester?>> = Flux.fromIterable(users.toList())

    fun getSocketOfUser(user: User): Mono<RSocketRequester> = Mono.justOrEmpty(users[user])

    fun isUserInConference(user: User): Mono<Boolean> {
        return conferenceStorage.isUserInConference(user)
    }


}
