package de.thm.mni.ii.classroom.model.classroom

import de.thm.mni.ii.classroom.exception.api.InvalidMeetingPasswordException
import de.thm.mni.ii.classroom.exception.classroom.ConferenceNotFoundException
import de.thm.mni.ii.classroom.exception.classroom.TicketAlreadyExistsException
import de.thm.mni.ii.classroom.exception.classroom.TicketNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.messaging.rsocket.RSocketRequester
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
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
) : ClassroomInfo(classroomId, classroomName) {

    private val logger = LoggerFactory.getLogger(DigitalClassroom::class.java)

    private val users = HashMap<User, RSocketRequester?>()
    private val tickets = HashSet<Ticket>()
    private val nextTicketId = AtomicLong(10000L)
    private val conferenceStorage = ConferenceStorage()

    val creationTimestamp: ZonedDateTime = ZonedDateTime.now()

    fun hasUserJoined() = users.filterValues { it != null }.isNotEmpty()
    fun hasBeenForciblyEnded() = false
    fun getDuration() = ChronoUnit.MINUTES.between(creationTimestamp, ZonedDateTime.now())

    fun doesUserExist(user: User): Boolean = users.contains(user)

    fun authenticateAssignRole(password: String, user: User): Mono<User> {
        return Mono.defer {
            when (password) {
                studentPassword -> user.userRole = UserRole.STUDENT
                teacherPassword -> user.userRole = UserRole.TEACHER
                tutorPassword -> user.userRole = UserRole.TUTOR
                else -> throw InvalidMeetingPasswordException(classroomId)
            }
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
        return tickets.toFlux().doOnNext {
            logger.info("Ticket: ${it.description}")
        }
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

    fun assignTicket(ticket: Ticket, newAssignee: User): Mono<Pair<Ticket, DigitalClassroom>> {
        return Mono.justOrEmpty(tickets.find { it == ticket })
            .switchIfEmpty(Mono.error(TicketNotFoundException(ticket)))
            .map { it.apply { assignee = newAssignee } }
            .map { Pair(it, this) }
    }

    fun deleteTicket(ticket: Ticket): Mono<Pair<Ticket, DigitalClassroom>> {
        return Mono.justOrEmpty(tickets.find { it == ticket })
            .switchIfEmpty(Mono.error(TicketNotFoundException(ticket)))
            .doOnNext { tickets.remove(it) }
            .map { Pair(it, this) }
    }

    fun getUsers(): Set<User> {
        return users.keys
    }

    fun getUsersFlux(): Flux<User> {
        return getUsers().toFlux()
    }

    fun getUserDisplays(): Flux<UserDisplay> {
        return this.getUsersFlux().map { user ->
            UserDisplay(user, conferenceStorage.getConferencesOfUser(user).lastOrNull())
        }
    }

    fun getConferencesOfUser(user: User): Flux<Conference> {
        return Flux.fromIterable(conferenceStorage.getConferencesOfUser(user))
    }

    fun getConferences(): Flux<Conference> {
        return conferenceStorage.getConferences()
    }

    fun saveConference(conference: Conference): Mono<Conference> {
        return conferenceStorage.createConference(conference)
    }

    fun joinUserToConference(conference: Conference, user: User): Mono<User> {
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

    fun getConference(conferenceId: String): Mono<Conference> {
        return conferenceStorage.getConference(conferenceId).toMono()
            .switchIfEmpty(Mono.error(ConferenceNotFoundException(conferenceId)))
    }

    fun leaveConference(user: User, conference: Conference) {
        this.conferenceStorage.leaveConference(user, conference)
    }

    fun getUsersOfConference(conference: Conference): Flux<User> {
        return Flux.fromIterable(conferenceStorage.getUsersOfConference(conference))
    }

    fun getLatestConferenceOfUser(user: User) = conferenceStorage.getLatestConferenceOfUser(user).toMono()

    fun deleteConference(conference: Conference): Mono<Conference> {
        return this.conferenceStorage.deleteConference(conference)
    }
}
