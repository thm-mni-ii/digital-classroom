package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.model.Conference
import de.thm.mni.ii.classroom.model.User
import de.thm.mni.ii.classroom.security.classroom.ClassroomAuthentication
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class ConferenceService(private val classroomInstanceService: ClassroomInstanceService,
                        private val upstreamBBBService: UpstreamBBBService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun getConferencesOfClassroom(auth: ClassroomAuthentication): Flux<Conference> {
        return classroomInstanceService
            .getClassroomInstance(auth.getClassroomId())
            .getConferences()
    }

    fun createConference(auth: ClassroomAuthentication): Mono<Conference> {
        val classroom = classroomInstanceService.getClassroomInstance(auth.getClassroomId())
        return upstreamBBBService
            .createConference(classroom, auth.user!!)
            .doOnNext { conference ->
                logger.info("Created conference ${conference.conferenceId} in classroom ${classroom.classroomName}!")
                classroom.saveConference(conference)
            }
    }

    fun joinUser(auth: ClassroomAuthentication, conference: Conference): Mono<String> {
        val classroom = classroomInstanceService.getClassroomInstance(auth.getClassroomId())
        return upstreamBBBService
            .joinConference(conference, auth.user!!, true)
            .doOnNext {
                logger.info("${auth.user.fullName} joined conference ${conference.conferenceId}!")
                classroom.joinUserToConference(conference, auth.user)
            }

    }

    fun joinConferenceOfUser(auth: ClassroomAuthentication, user: User): Mono<String> {
        val classroom = classroomInstanceService.getClassroomInstance(auth.getClassroomId())
        return classroom.getConferenceOfUser(user).flatMap {
            joinUser(auth, it)
        }
    }

}