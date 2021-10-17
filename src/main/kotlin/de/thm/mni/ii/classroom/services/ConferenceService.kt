package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.event.ConferenceAction
import de.thm.mni.ii.classroom.event.ConferenceEvent
import de.thm.mni.ii.classroom.event.InvitationEvent
import de.thm.mni.ii.classroom.exception.classroom.InvitationException
import de.thm.mni.ii.classroom.model.classroom.Conference
import de.thm.mni.ii.classroom.model.classroom.ConferenceInfo
import de.thm.mni.ii.classroom.model.classroom.DigitalClassroom
import de.thm.mni.ii.classroom.model.classroom.JoinLink
import de.thm.mni.ii.classroom.model.classroom.UserCredentials
import de.thm.mni.ii.classroom.security.exception.UnauthorizedException
import de.thm.mni.ii.classroom.util.component1
import de.thm.mni.ii.classroom.util.component2
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toMono
import reactor.util.function.Tuple2
import java.time.Duration
import java.time.ZonedDateTime

@Component
class ConferenceService(
    private val classroomInstanceService: ClassroomInstanceService,
    private val upstreamBBBService: UpstreamBBBService,
) {
    private val logger = LoggerFactory.getLogger(ConferenceService::class.java)

    fun createConference(userCredentials: UserCredentials, conferenceInfo: ConferenceInfo): Mono<ConferenceInfo> {
        return classroomInstanceService.getClassroomInstance(userCredentials.classroomId)
            .zipWith(upstreamBBBService.createConference(userCredentials, conferenceInfo))
            .doOnNext { (classroom, conference) ->
                logger.info("Created conference ${conference.conferenceId} in classroom ${classroom.classroomName}!")
                classroom.sendToAll(ConferenceEvent(conference.toConferenceInfo(), ConferenceAction.CREATE)).subscribe()
            }.flatMap { (classroom, conference) ->
                classroom.conferences.createConference(conference)
            }.map(Conference::toConferenceInfo)
    }

    fun joinConference(userCredentials: UserCredentials, conferenceInfo: ConferenceInfo): Mono<JoinLink> {
        return classroomInstanceService.getClassroomInstance(userCredentials.classroomId)
            .zipWhen { classroom ->
                classroom.conferences.getConference(conferenceInfo.conferenceId!!)
            }.flatMap { (classroom, conference) ->
                joinUser(userCredentials, conference, classroom)
            }
    }

    private fun joinUser(userCredentials: UserCredentials, conference: Conference, classroom: DigitalClassroom): Mono<JoinLink> {
        val user = classroom.getUser(userCredentials.userId)
        val asModerator = user.isPrivileged() || conference.creator == user
        return upstreamBBBService.joinConference(conference, user, asModerator)
            .zipWith(classroom.conferences.joinUser(conference, userCredentials))
            .doOnNext { (_, conference) ->
                logger.info("${userCredentials.fullName} joins conference ${conference.conferenceId}!")
                classroom.sendToAll(
                    ConferenceEvent(conference.toConferenceInfo(), ConferenceAction.USER_CHANGE)
                ).subscribe()
            }.map(Tuple2<JoinLink, Conference>::getT1)
    }

    fun endConferenceManually(userCredentials: UserCredentials?, conferenceInfo: ConferenceInfo): Mono<Void> {
        if (userCredentials != null && !userCredentials.isPrivileged() && userCredentials != conferenceInfo.creator) {
            logger.warn("User ${userCredentials.fullName} is not authorized to end ${conferenceInfo.conferenceName}!")
            return Mono.empty()
        }
        return endConference(conferenceInfo)
    }

    private fun endConference(conferenceInfo: ConferenceInfo): Mono<Void> {
        return this.classroomInstanceService.getClassroomInstance(conferenceInfo.classroomId)
            .zipWhen { classroom ->
                classroom.conferences.getConference(conferenceInfo.conferenceId!!)
            }.delayUntil { (_, conference) ->
                this.upstreamBBBService.endConference(conference)
            }.delayUntil { (classroom, conference) ->
                classroom.conferences.deleteConference(conference)
            }.flatMap { (classroom, conference) ->
                val conferenceEvent = ConferenceEvent(conference.toConferenceInfo(), ConferenceAction.CLOSE)
                classroom.sendToAll(conferenceEvent)
            }
    }

    fun changeVisibility(userCredentials: UserCredentials, conferenceInfo: ConferenceInfo) {
        classroomInstanceService.getClassroomInstance(userCredentials.classroomId)
            .map {
                if (userCredentials != conferenceInfo.creator) {
                    throw UnauthorizedException("Only the creator may hide or publish a conference!")
                } else {
                    it
                }
            }.zipWhen { classroom ->
                classroom.conferences.changeVisibility(conferenceInfo)
            }.flatMap { (classroom, conference) ->
                val event = ConferenceEvent(conference.toConferenceInfo(), ConferenceAction.VISIBILITY)
                classroom.sendToAll(event)
            }.subscribe()
    }

    fun forwardInvitation(userCredentials: UserCredentials, invitationEvent: InvitationEvent): Mono<Void> {
        if (invitationEvent.inviter != userCredentials) return Mono.error(InvitationException(userCredentials, invitationEvent))
        return classroomInstanceService.getClassroomInstance(userCredentials.classroomId)
            .flatMap { classroom ->
                classroom.sendInvitation(invitationEvent)
            }
    }

    fun leaveConference(userCredentials: UserCredentials, conferenceInfo: ConferenceInfo): Mono<Void> {
        return classroomInstanceService.getClassroomInstance(userCredentials.classroomId)
            .zipWhen { classroom ->
                classroom.conferences.getConference(conferenceInfo.conferenceId!!)
            }.flatMap { (classroom, conference) ->
                Mono.zip(classroom.toMono(), classroom.conferences.leaveConference(userCredentials, conference))
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

    @Suppress("unused")
    fun removeUserFromAllConferences(classroom: DigitalClassroom, userCredentials: UserCredentials): Mono<Void> {
        return classroom.conferences.removeFromConferences(userCredentials)
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
            .publishOn(Schedulers.boundedElastic())
            .collectList()
            .zipWhen { conferences -> this.upstreamBBBService.syncMeetings(classroom, conferences) }
            .delayUntil { (original, syncedConferences) ->
                // End all conferences (delete from local storage) that were stored, but are not known by BBB.
                Flux.concat(original.minus(syncedConferences).filter { conference ->
                    ZonedDateTime.now().isAfter(conference.creationTimestamp.plusSeconds(30))
                }.map(Conference::toConferenceInfo)
                    .map(this::endConference))
            }.map { (_, syncedConferences) ->
                // Schedule deletion if conference is empty.
                syncedConferences.filter { it.attendees.isEmpty() }.forEach { conference ->
                    this.scheduleConferenceDeletion(classroom, conference, 30)
                }
                syncedConferences
            }.flatMap(classroom.conferences::updateConferences)
    }

    fun scheduleConferenceDeletion(classroom: DigitalClassroom, conference: Conference, delaySeconds: Long = 90) {
        logger.debug("Conference ${conference.conferenceId} scheduled for deletion if still empty in $delaySeconds seconds!")
        Mono.just(conference)
            .delayElement(Duration.ofSeconds(delaySeconds))
            .flatMap { classroom.conferences.getUsersOfConference(it).hasElements() }
            // Stop if users rejoined the conference!
            .filter { usersJoined ->
                if (usersJoined) logger.debug("Users rejoined to conference ${conference.conferenceId}. Abort deletion.")
                else logger.debug("Conference ${conference.conferenceId} is still empty. Deleting...")
                !usersJoined
            }.flatMap {
                this.endConference(conference.toConferenceInfo())
            }.subscribe()
    }
}
