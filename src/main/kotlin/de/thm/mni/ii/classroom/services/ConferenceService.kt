package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.event.ConferenceAction
import de.thm.mni.ii.classroom.event.ConferenceEvent
import de.thm.mni.ii.classroom.event.UserAction
import de.thm.mni.ii.classroom.event.UserEvent
import de.thm.mni.ii.classroom.model.classroom.*
import de.thm.mni.ii.classroom.security.jwt.ClassroomAuthentication
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

import de.thm.mni.ii.classroom.util.component1
import de.thm.mni.ii.classroom.util.component2

@Component
class ConferenceService(private val classroomInstanceService: ClassroomInstanceService,
                        private val upstreamBBBService: UpstreamBBBService,
                        private val eventSenderService: ClassroomEventSenderService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

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
                Mono.zip(Mono.just(classroom), classroom.getConferenceOfUser(conferencingUser))
            }.flatMap { (classroom, conference) ->
                joinUser(joiningUser, conference, classroom)
            }
    }

    private fun joinUser(user: User, conference: Conference, classroom: DigitalClassroom): Mono<JoinLink> {
        return upstreamBBBService.joinConference(conference, user, true)
            .doOnSuccess {
                logger.info("${user.fullName} joins conference ${conference.conferenceId}!")
                classroom.joinUserToConference(conference, user).subscribe()
                eventSenderService.sendToAll(
                    classroom,
                    UserEvent(user, true, conference.conferenceId, UserAction.JOIN_CONFERENCE)
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

}
