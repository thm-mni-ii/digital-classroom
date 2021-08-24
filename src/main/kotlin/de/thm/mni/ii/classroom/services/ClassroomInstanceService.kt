package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.exception.api.ClassroomNotFoundException
import de.thm.mni.ii.classroom.model.classroom.DigitalClassroom
import de.thm.mni.ii.classroom.model.classroom.User
import de.thm.mni.ii.classroom.model.api.JoinRoomBBBResponse
import de.thm.mni.ii.classroom.properties.ClassroomProperties
import de.thm.mni.ii.classroom.security.classroom.ClassroomUserDetailsRepository
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.net.URL
import java.util.*
import kotlin.collections.HashMap

/**
 * Central service for managing and creating all DigitalClassroomInstances.
 */
@Service
class ClassroomInstanceService(private val classroomProperties: ClassroomProperties,
                               private val serverProperties: ServerProperties,
                               private val classroomUserDetailsRepository: ClassroomUserDetailsRepository
) {

    private val classrooms = HashMap<String, DigitalClassroom>()

    /**
     * Creates a new classroom instance and stores it inside the classroom map.
     * If a classroom with the given id already exists,
     * the existing DigitalClassroomInstance is returned.
     * @param classroomId the Id of the classroom
     * @param studentPassword password for students given by the downstream service
     * @param tutorPassword password for tutors given by the downstream service
     * @param teacherPassword password for teachers given by the downstream service
     * @param classroomName informal name given to the classroom.
     */
    fun createNewClassroomInstance(classroomId: String,
                                   studentPassword: String?,
                                   tutorPassword: String?,
                                   teacherPassword: String?,
                                   classroomName: String?
    ): DigitalClassroom {
        return classrooms.computeIfAbsent(classroomId) { id ->
            DigitalClassroom(
                id,
                studentPassword = studentPassword ?: RandomStringUtils.randomAlphanumeric(30),
                tutorPassword = tutorPassword ?: RandomStringUtils.randomAlphanumeric(30),
                teacherPassword = teacherPassword ?: RandomStringUtils.randomAlphanumeric(30),
                classroomName = classroomName ?: "Digital Classroom - ${UUID.randomUUID()}",
                internalClassroomId = "${RandomStringUtils.randomAlphanumeric(40)}-${RandomStringUtils.randomAlphanumeric(13)}"
            )
        }
    }

    fun getClassroomInstance(classroomId: String): DigitalClassroom {
        return classrooms[classroomId] ?: throw ClassroomNotFoundException(classroomId)
    }

    // TODO: Separate concerns. This should not return a BBBResponse, but only the URL to join the user.
    fun joinUser(classroomId: String, password: String, user: User): Mono<JoinRoomBBBResponse> {
        val classroom = classrooms[classroomId]
        if (classroom == null) {
            throw ClassroomNotFoundException(classroomId)
        } else {
            return Mono.create {
                val joinedUser = classroom.joinUser(password, user)
                val sessionToken = RandomStringUtils.randomAlphanumeric(16)
                classroomUserDetailsRepository.insertValidToken(sessionToken, joinedUser)
                val url = URL("${classroomProperties.host}${classroomProperties.prefixPath}/classroom/join?sessionToken=$sessionToken").toString()
                it.success(
                    JoinRoomBBBResponse(
                        success = true,
                        meetingID = classroom.internalClassroomId,
                        sessionToken = sessionToken,
                        url = url,
                        userID = user.userId
                    )
                )
            }

        }
    }

    fun isRunning(classroomId: String) = classrooms.containsKey(classroomId)

}
