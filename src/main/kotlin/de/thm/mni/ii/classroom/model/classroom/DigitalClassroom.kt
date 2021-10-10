package de.thm.mni.ii.classroom.model.classroom

import de.thm.mni.ii.classroom.event.ClassroomEvent
import de.thm.mni.ii.classroom.event.InvitationEvent
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
import java.net.URL
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
    classroomName: String,
    logoutUrl: URL?
) : ClassroomInfo(classroomId, classroomName, logoutUrl) {

    private val logger = LoggerFactory.getLogger(DigitalClassroom::class.java)

    private val preAuthUserData = HashMap<UserCredentials, User>()
    private val users = HashMap<User, RSocketRequester?>()
    private val tickets = HashSet<Ticket>()
    private val nextTicketId = AtomicLong(1L)
    val conferences = ConferenceStorage()

    val creationTimestamp: ZonedDateTime = ZonedDateTime.now()

    fun hasUserJoined() = users.isNotEmpty()
    fun hasBeenForciblyEnded() = false
    fun getDuration() = ChronoUnit.MINUTES.between(creationTimestamp, ZonedDateTime.now())
    fun getUser(userId: String): User = users.keys.find { it.userId == userId }!!
    fun doesUserExist(userCredentials: UserCredentials): Boolean = users.contains(userCredentials)

    fun authenticateAssignRole(password: String, userCredentials: UserCredentials): Mono<UserCredentials> {
        return Mono.defer {
            when (password) {
                studentPassword -> userCredentials.userRole = UserRole.STUDENT
                teacherPassword -> userCredentials.userRole = UserRole.TEACHER
                tutorPassword -> userCredentials.userRole = UserRole.TUTOR
                else -> throw InvalidMeetingPasswordException(classroomId)
            }
            this.preAuthUserData[userCredentials] = user
            Mono.just(userCredentials)
        }
    }

    fun connectSocket(userCredentials: UserCredentials, socketRequester: RSocketRequester): Mono<User> {
        val user = this.preAuthUserData.remove(userCredentials)!!
        users[user] = socketRequester
        return Mono.just(user)
    }

    fun disconnectSocket(userCredentials: UserCredentials): Mono<User> {
        val user = users.keys.find { it == userCredentials }!!
        val socket = users.remove(userCredentials)
        socket?.rsocket()?.dispose()
        this.preAuthUserData[userCredentials] = user
        return Mono.just(user)
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

    fun assignTicket(ticket: Ticket, newAssignee: UserCredentials): Mono<Pair<Ticket, DigitalClassroom>> {
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

    fun getSockets(): Flux<Pair<User, RSocketRequester?>> = Flux.fromIterable(users.toList())

    fun getSocketOfUser(userCredentials: UserCredentials): Mono<RSocketRequester> = Mono.just(users[userCredentials]!!)

    fun isUserInConference(user: User): Mono<Boolean> {
        return conferences.isUserInConference(user)
    }

    fun getConference(conferenceId: String): Mono<Conference> {
        return conferences.getConference(conferenceId).toMono()
            .switchIfEmpty(Mono.error(ConferenceNotFoundException(conferenceId)))
    }

    fun leaveConference(user: User, conference: Conference): Mono<Conference> {
        return this.conferences.leaveConference(user, conference).toMono()
    }

    fun deleteConference(conference: Conference): Mono<Conference> {
        return this.conferences.deleteConference(conference)
    }

    fun changeVisibility(user: User): Mono<User> {
        return this.users.keys
            .find { it == user }
            .also { it?.visible = user.visible }
            .toMono()
    }

    fun sendInvitation(invitationEvent: InvitationEvent): Mono<Void> {
        return getSocketOfUser(invitationEvent.invitee).doOnNext { requester ->
            logger.trace("${invitationEvent.inviter.fullName} invites ${invitationEvent.invitee.fullName} to conference!")
            fireAndForget(invitationEvent, requester)
        }.then()
    }

    fun sendToAll(event: ClassroomEvent): Mono<Void> {
        return getSockets().doOnNext { (user, requester) ->
            if (requester != null) {
                logger.trace("sending to ${user.fullName}")
                fireAndForget(event, requester)
            }
        }.then()
    }

    private fun fireAndForget(event: ClassroomEvent, requester: RSocketRequester) {
        requester.route("").data(event).send().subscribe().dispose()
    }

    fun savePreAuthUserData(user: User) {
        assert(user.classroomId == this.classroomId)
        this.preAuthUserData[user] = user
    }
}
