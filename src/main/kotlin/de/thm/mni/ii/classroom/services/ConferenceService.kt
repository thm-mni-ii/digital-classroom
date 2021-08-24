package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.model.classroom.Conference
import de.thm.mni.ii.classroom.model.classroom.User
import de.thm.mni.ii.classroom.security.classroom.ClassroomAuthentication
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

import de.thm.mni.ii.classroom.util.component1
import de.thm.mni.ii.classroom.util.component2

@Component
class ConferenceService(private val classroomInstanceService: ClassroomInstanceService,
                        private val upstreamBBBService: UpstreamBBBService) {

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

    fun joinUser(auth: ClassroomAuthentication, conference: Conference): Mono<String> {
        return classroomInstanceService.getClassroomInstance(auth.getClassroomId()).flatMap { classroom ->
            Mono.zip(Mono.just(classroom), upstreamBBBService.joinConference(conference, auth.user!!, true))
        }.map { (classroom, conferenceLink) ->
            logger.info("${auth.user!!.fullName} joined conference ${conference.conferenceId}!")
            classroom.joinUserToConference(conference, auth.user)
            conferenceLink
        }
    }

    fun joinConferenceOfUser(auth: ClassroomAuthentication, user: User): Mono<String> {
        return classroomInstanceService.getClassroomInstance(auth.getClassroomId()).flatMap { classroom ->
            classroom.getConferenceOfUser(user)
                .flatMap { conference ->
                    joinUser(auth, conference)
                }
        }
    }

    fun getUsersInConferences(auth: ClassroomAuthentication): Flux<User> {
        return classroomInstanceService.getClassroomInstance(auth.getClassroomId()).flatMapMany {
            it.getUsersInConferences()
        }
    }

}
