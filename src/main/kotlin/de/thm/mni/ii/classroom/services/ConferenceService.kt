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

    fun createConference(user: User, conferenceInfo: ConferenceInfo): Mono<ConferenceInfo> {
        return classroomInstanceService.getClassroomInstance(user.classroomId)
            .flatMap { classroom ->
                Mono.zip(
                    Mono.just(classroom),
                    upstreamBBBService.createConference(user, conferenceInfo)
                )
            }.doOnNext { (classroom, conference) ->
                logger.info("Created conference ${conference.conferenceId} in classroom ${classroom.classroomName}!")
                eventSenderService.sendToAll(classroom, ConferenceEvent(ConferenceInfo(conference), ConferenceAction.CREATE)).subscribe()
            }.flatMap { (classroom, conference) ->
                classroom.saveConference(conference)
            }.map(::ConferenceInfo)
    }

    fun joinConference(user: User, conferenceInfo: ConferenceInfo): Mono<JoinLink> {
        return classroomInstanceService.getClassroomInstance(user.classroomId)
            .flatMap { classroom ->
                Mono.zip(Mono.just(classroom), classroom.getConference(conferenceInfo.conferenceId!!))
            }.flatMap { (classroom, conference) ->
                joinUser(user, conference, classroom)
            }
    }

    fun joinConferenceOfUser(joiningUser: User, conferencingUser: User): Mono<JoinLink> {
        return classroomInstanceService.getClassroomInstance(joiningUser.classroomId)
            .flatMap { classroom ->
                Mono.zip(Mono.just(classroom), classroom.getConferencesOfUser(conferencingUser).last())
            }.flatMap { (classroom, conference) ->
                joinUser(joiningUser, conference!!, classroom)
            }
    }

    private fun joinUser(user: User, conference: Conference, classroom: DigitalClassroom): Mono<JoinLink> {
        return upstreamBBBService.joinConference(conference, user, true)
            .doOnSuccess {
                logger.info("${user.fullName} joins conference ${conference.conferenceId}!")
                classroom.joinUserToConference(conference, user).subscribe()
                eventSenderService.sendToAll(
                    classroom,
                    ConferenceEvent(conference.toConferenceInfo(), ConferenceAction.USER_CHANGE)
                ).subscribe()
            }
    }

    fun getUsersInConferences(auth: ClassroomAuthentication): Flux<User> {
        return classroomInstanceService.getClassroomInstance(auth.getClassroomId()).flatMapMany {
            it.getUsersInConferences()
        }
    }

    fun endConference(user: User, conferenceInfo: ConferenceInfo) {
        TODO("Not yet implemented")
    }

    fun hideConference(user: User, conferenceInfo: ConferenceInfo) {
        TODO("Not yet implemented")
    }

    fun publishConference(user: User, conferenceInfo: ConferenceInfo) {
        TODO("Not yet implemented")
    }

    fun forwardInvitation(user: User, invitationEvent: InvitationEvent): Mono<Void> {
        if (invitationEvent.inviter != user) return Mono.error(InvitationException(user, invitationEvent))
        return classroomInstanceService.getClassroomInstance(user.classroomId)
            .doOnNext { classroom ->
                eventSenderService.sendInvitation(classroom, invitationEvent).subscribe()
            }.then()
    }

    fun leaveConference(user: User, conferenceInfo: ConferenceInfo): Mono<Void> {
        val classroom = classroomInstanceService.getClassroomInstanceSync(user.classroomId)
        return classroom.getConference(conferenceInfo.conferenceId!!)
            .flatMap { conference ->
                classroom.leaveConference(user, conference)
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
