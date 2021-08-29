package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.model.classroom.Conference
import de.thm.mni.ii.classroom.model.classroom.User
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

    fun getConferencesOfClassroom(auth: ClassroomAuthentication): Flux<Conference> {
        return classroomInstanceService
            .getClassroomInstance(auth.getClassroomId()).flatMapMany {
                it.getConferences()
            }
    }

    fun createConference(auth: ClassroomAuthentication): Mono<Conference> {
        return classroomInstanceService.getClassroomInstance(auth.getClassroomId()).flatMap { classroom ->
            Mono.zip(Mono.just(classroom), upstreamBBBService.createConference(classroom, auth.user!!))
        }.map { (classroom, conference) ->
            logger.info("Created conference ${conference.conferenceId} in classroom ${classroom.classroomName}!")
            classroom.saveConference(conference)
            conference
        }
    }

    fun joinUser(user: User, conference: Conference): Mono<String> {
        return classroomInstanceService.getClassroomInstance(user.classroomId).flatMap { classroom ->
            Mono.zip(Mono.just(classroom), upstreamBBBService.joinConference(conference, user, true))
        }.doOnNext { (classroom, _) ->
            logger.info("${user.fullName} joins conference ${conference.conferenceId}!")
            classroom.joinUserToConference(conference, user).subscribe()
        }.map { (_, conferenceLink) ->
            conferenceLink
        }
    }

    fun joinConferenceOfUser(joiningUser: User, conferencingUser: User): Mono<String> {
        return classroomInstanceService.getClassroomInstance(joiningUser.classroomId).flatMap { classroom ->
            classroom.getConferenceOfUser(conferencingUser)
                .flatMap { conference ->
                    joinUser(joiningUser, conference)
                }
        }
    }

    fun getUsersInConferences(auth: ClassroomAuthentication): Flux<User> {
        return classroomInstanceService.getClassroomInstance(auth.getClassroomId()).flatMapMany {
            it.getUsersInConferences()
        }
    }

}
