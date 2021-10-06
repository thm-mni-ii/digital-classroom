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
import de.thm.mni.ii.classroom.security.jwt.ClassroomAuthentication
import de.thm.mni.ii.classroom.util.component1
import de.thm.mni.ii.classroom.util.component2
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

@Component
class ConferenceService(
    private val classroomInstanceService: ClassroomInstanceService,
    private val upstreamBBBService: UpstreamBBBService,
    private val eventSenderService: ClassroomEventSenderService
) {

    private val logger = LoggerFactory.getLogger(ConferenceService::class.java)

    fun createConference(userCredentials: UserCredentials, conferenceInfo: ConferenceInfo): Mono<ConferenceInfo> {
        return classroomInstanceService.getClassroomInstance(userCredentials.classroomId)
            .flatMap { classroom ->
                Mono.zip(
                    Mono.just(classroom),
                    upstreamBBBService.createConference(userCredentials, conferenceInfo)
                )
            }.doOnNext { (classroom, conference) ->
                logger.info("Created conference ${conference.conferenceId} in classroom ${classroom.classroomName}!")
                eventSenderService.sendToAll(classroom, ConferenceEvent(ConferenceInfo(conference), ConferenceAction.CREATE)).subscribe()
            }.flatMap { (classroom, conference) ->
                classroom.saveConference(conference)
            }.map(::ConferenceInfo)
    }

    fun joinConference(userCredentials: UserCredentials, conferenceInfo: ConferenceInfo): Mono<JoinLink> {
        return classroomInstanceService.getClassroomInstance(userCredentials.classroomId)
            .flatMap { classroom ->
                Mono.zip(Mono.just(classroom), classroom.getConference(conferenceInfo.conferenceId!!))
            }.flatMap { (classroom, conference) ->
                joinUser(userCredentials, conference, classroom)
            }
    }

    fun joinConferenceOfUser(joiningUserCredentials: UserCredentials, conferencingUserCredentials: UserCredentials): Mono<JoinLink> {
        return classroomInstanceService.getClassroomInstance(joiningUserCredentials.classroomId)
            .flatMap { classroom ->
                Mono.zip(Mono.just(classroom), classroom.getConferencesOfUser(conferencingUserCredentials).last())
            }.flatMap { (classroom, conference) ->
                joinUser(joiningUserCredentials, conference!!, classroom)
            }
    }

    private fun joinUser(userCredentials: UserCredentials, conference: Conference, classroom: DigitalClassroom): Mono<JoinLink> {
        return upstreamBBBService.joinConference(conference, userCredentials, true)
            .doOnSuccess {
                logger.info("${userCredentials.fullName} joins conference ${conference.conferenceId}!")
                classroom.joinUserToConference(conference, userCredentials).subscribe()
                eventSenderService.sendToAll(
                    classroom,
                    ConferenceEvent(conference.toConferenceInfo(), ConferenceAction.USER_CHANGE)
                ).subscribe()
            }
    }

    fun getUsersInConferences(auth: ClassroomAuthentication): Flux<UserCredentials> {
        return classroomInstanceService.getClassroomInstance(auth.getClassroomId()).flatMapMany {
            it.getUsersInConferences()
        }
    }

    fun endConference(userCredentials: UserCredentials, conferenceInfo: ConferenceInfo) {
        TODO("Not yet implemented")
    }

    fun hideConference(userCredentials: UserCredentials, conferenceInfo: ConferenceInfo) {
        TODO("Not yet implemented")
    }

    fun publishConference(userCredentials: UserCredentials, conferenceInfo: ConferenceInfo) {
        TODO("Not yet implemented")
    }

    fun forwardInvitation(userCredentials: UserCredentials, invitationEvent: InvitationEvent): Mono<Void> {
        if (invitationEvent.inviter != userCredentials) return Mono.error(InvitationException(userCredentials, invitationEvent))
        return classroomInstanceService.getClassroomInstance(userCredentials.classroomId)
            .doOnNext { classroom ->
                eventSenderService.sendInvitation(classroom, invitationEvent).subscribe()
            }.then()
    }

    fun leaveConference(userCredentials: UserCredentials, conferenceInfo: ConferenceInfo): Mono<Void> {
        val classroom = classroomInstanceService.getClassroomInstanceSync(userCredentials.classroomId)
        return classroom.getConference(conferenceInfo.conferenceId!!)
            .flatMap { conference ->
                classroom.leaveConference(userCredentials, conference)
            }.doOnNext { conference ->
                if (conference.attendees.isEmpty()) {
                    this.scheduleConferenceDeletion(classroom, conference, 20)
                }
            }.flatMap { conference ->
                eventSenderService.sendToAll(
                    classroom,
                    ConferenceEvent(conference.toConferenceInfo(), ConferenceAction.USER_CHANGE)
                )
            }
    }

    fun scheduleConferenceDeletion(classroom: DigitalClassroom, conference: Conference, delaySeconds: Long) {
        logger.debug("Conference ${conference.conferenceId} scheduled for deletion if still empty in $delaySeconds seconds!")
        Mono.just(conference)
            .delayElement(Duration.ofSeconds(delaySeconds))
            .flatMap { classroom.getUsersOfConference(it).hasElements() }
            .flatMap { usersJoined ->
                if (!usersJoined) {
                    logger.debug("Conference ${conference.conferenceId} is still empty. Deleting...")
                    Mono.just(usersJoined)
                } else {
                    logger.debug("Users rejoined to conference ${conference.conferenceId}. Abort deletion.")
                    Mono.empty()
                }
            }.flatMap {
                upstreamBBBService.endConference(conference)
            }.flatMap {
                classroom.deleteConference(conference)
            }.flatMap {
                val conferenceEvent = ConferenceEvent(conference.toConferenceInfo(), ConferenceAction.CLOSE)
                eventSenderService.sendToAll(classroom, conferenceEvent)
            }.subscribe()
    }
}
