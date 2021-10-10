package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.event.ConferenceAction
import de.thm.mni.ii.classroom.event.ConferenceEvent
import de.thm.mni.ii.classroom.event.InvitationEvent
import de.thm.mni.ii.classroom.exception.classroom.InvitationException
import de.thm.mni.ii.classroom.model.classroom.Conference
import de.thm.mni.ii.classroom.model.classroom.ConferenceInfo
import de.thm.mni.ii.classroom.model.classroom.DigitalClassroom
import de.thm.mni.ii.classroom.model.classroom.JoinLink
import de.thm.mni.ii.classroom.model.classroom.User
import de.thm.mni.ii.classroom.security.exception.UnauthorizedException
import de.thm.mni.ii.classroom.security.jwt.ClassroomAuthentication
import de.thm.mni.ii.classroom.util.component1
import de.thm.mni.ii.classroom.util.component2
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.util.function.Tuple2
import java.time.Duration
import java.util.Timer
import kotlin.concurrent.schedule

@Component
class ConferenceService(
    private val classroomInstanceService: ClassroomInstanceService,
    private val upstreamBBBService: UpstreamBBBService,
) {

    private val logger = LoggerFactory.getLogger(ConferenceService::class.java)

    fun createConference(user: User, conferenceInfo: ConferenceInfo): Mono<ConferenceInfo> {
        return classroomInstanceService.getClassroomInstance(user.classroomId)
            .zipWith(upstreamBBBService.createConference(user, conferenceInfo))
            .doOnNext { (classroom, conference) ->
                logger.info("Created conference ${conference.conferenceId} in classroom ${classroom.classroomName}!")
                classroom.sendToAll(ConferenceEvent(ConferenceInfo(conference), ConferenceAction.CREATE)).subscribe()
            }.flatMap { (classroom, conference) ->
                classroom.conferences.createConference(conference)
            }.map(::ConferenceInfo)
    }

    fun joinConference(user: User, conferenceInfo: ConferenceInfo): Mono<JoinLink> {
        return classroomInstanceService.getClassroomInstance(user.classroomId)
            .zipWhen { classroom ->
                classroom.conferences.getConference(conferenceInfo.conferenceId!!)
            }.flatMap { (classroom, conference) ->
                joinUser(user, conference, classroom)
            }
    }

    fun joinConferenceOfUser(joiningUser: User, conferencingUser: User): Mono<JoinLink> {
        return classroomInstanceService.getClassroomInstance(joiningUser.classroomId)
            .zipWhen { classroom ->
                classroom.conferences.getConferencesOfUser(conferencingUser).last()
            }.flatMap { (classroom, conference) ->
                joinUser(joiningUser, conference, classroom)
            }
    }

    private fun joinUser(user: User, conference: Conference, classroom: DigitalClassroom): Mono<JoinLink> {
        return upstreamBBBService.joinConference(conference, user, true)
            .zipWith(classroom.conferences.joinUser(conference, user))
            .doOnNext { (_, conference) ->
                logger.info("${user.fullName} joins conference ${conference.conferenceId}!")
                classroom.sendToAll(
                    ConferenceEvent(conference.toConferenceInfo(), ConferenceAction.USER_CHANGE)
                ).subscribe()
            }.map(Tuple2<JoinLink, Conference>::getT1)
    }

    fun getUsersInConferences(auth: ClassroomAuthentication): Flux<User> {
        return classroomInstanceService.getClassroomInstance(auth.getClassroomId())
            .flatMapMany {
                it.conferences.getUsersInConferences()
            }
    }

    fun endConference(user: User, conferenceInfo: ConferenceInfo) {
        TODO("Not yet implemented")
    }

    fun changeVisibility(user: User, conferenceInfo: ConferenceInfo): Mono<Void> {
        return classroomInstanceService.getClassroomInstance(user.classroomId)
            .map {
                if (user != conferenceInfo.creator) {
                    throw UnauthorizedException("Only the creator may hide or publish a conference!")
                } else {
                    it
                }
            }.zipWhen { classroom ->
                classroom.conferences.changeVisibility(conferenceInfo)
            }.flatMap { (classroom, conference) ->
                val event = ConferenceEvent(conference.toConferenceInfo(), ConferenceAction.VISIBILITY)
                classroom.sendToAll(event)
            }
    }

    fun forwardInvitation(user: User, invitationEvent: InvitationEvent): Mono<Void> {
        if (invitationEvent.inviter != user) return Mono.error(InvitationException(user, invitationEvent))
        return classroomInstanceService.getClassroomInstance(user.classroomId)
            .flatMap { classroom ->
                classroom.sendInvitation(invitationEvent)
            }
    }

    fun leaveConference(user: User, conferenceInfo: ConferenceInfo): Mono<Void> {
        return classroomInstanceService.getClassroomInstance(user.classroomId)
            .zipWhen { classroom ->
                classroom.conferences.getConference(conferenceInfo.conferenceId!!)
            }.flatMap { (classroom, conference) ->
                Mono.zip(classroom.toMono(), classroom.conferences.leaveConference(user, conference))
            }.doOnNext { (classroom, conferenceLeft) ->
                if (conferenceLeft.attendees.isEmpty()) {
                    this.scheduleConferenceDeletion(classroom, conferenceLeft)
                }
            }.flatMap { (classroom, conferenceLeft) ->
                classroom.sendToAll(
                    ConferenceEvent(conferenceLeft.toConferenceInfo(), ConferenceAction.USER_CHANGE)
                )
            }
    }

    fun removeUserFromAllConferences(classroom: DigitalClassroom, user: User): Mono<Void> {
        return classroom.conferences.removeFromConferences(user)
            .doOnNext { conference ->
                if (conference.attendees.isEmpty()) {
                    scheduleConferenceDeletion(classroom, conference)
                }
            }.flatMap { conference ->
                val confEvent = ConferenceEvent(conference.toConferenceInfo(), ConferenceAction.USER_CHANGE)
                classroom.sendToAll(confEvent)
            }.then()
    }

    fun updateConferences(classroom: DigitalClassroom): Mono<Void> {
        return classroom.conferences.getConferences()
            .collectList()
            .flatMapMany { conferences -> this.upstreamBBBService.syncMeetings(classroom, conferences) }
            .collectList()
            .flatMap(classroom.conferences::updateConferences)
    }

    fun scheduleConferenceDeletion(classroom: DigitalClassroom, conference: Conference, delaySeconds: Long = 20) {
        logger.debug("Conference ${conference.conferenceId} scheduled for deletion if still empty in $delaySeconds seconds!")
        Mono.just(conference)
            .delayElement(Duration.ofSeconds(delaySeconds))
            .delayUntil { this.updateConferences(classroom) }
            .flatMap { classroom.conferences.getUsersOfConference(it).hasElements() }
            // Stop if users rejoined the conference!
            .filter { usersJoined -> !usersJoined }
            .doOnTerminate {
                logger.debug("Users rejoined to conference ${conference.conferenceId}. Abort deletion.")
            }.flatMap {
                logger.debug("Conference ${conference.conferenceId} is still empty. Deleting...")
                upstreamBBBService.endConference(conference)
            }.flatMap {
                classroom.conferences.deleteConference(conference)
            }.flatMap {
                val conferenceEvent = ConferenceEvent(conference.toConferenceInfo(), ConferenceAction.CLOSE)
                classroom.sendToAll(conferenceEvent)
            }.subscribe()
    }
}
