package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.exception.ClassroomNotFoundException
import de.thm.mni.ii.classroom.model.DigitalClassroom
import de.thm.mni.ii.classroom.model.User
import de.thm.mni.ii.classroom.model.dto.JoinRoomBBBResponse
import de.thm.mni.ii.classroom.properties.ClassroomProperties
import de.thm.mni.ii.classroom.security.classroom.ClassroomUserDetailsRepository
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.stereotype.Service
import java.net.URL
import java.util.*
import kotlin.collections.HashMap

/**
 * Central service for managing and creating all DigitalClassroomInstances.
 */
@Service
class ClassroomInstanceManagingService(private val classroomProperties: ClassroomProperties,
                                       private val serverProperties: ServerProperties,
                                       private val classroomUserDetailsRepository: ClassroomUserDetailsRepository
) {

    private val classrooms = HashMap<String, DigitalClassroom>()

    /**
     * Creates a new classroom instance and stores it inside the classroom map.
     * If a classroom with the given id already exists,
     * the existing DigitalClassroomInstance is returned.
     * @param meetingID the meetingID of the classroom
     * @param attendeePW password for attendees given by the downstream service
     * @param moderatorPW password for moderators given by the downstream service
     * @param meetingName informal name given to the meeting
     */
    fun createNewClassroomInstance(meetingID: String,
                                   attendeePW: String?,
                                   assistantPW: String?,
                                   moderatorPW: String?,
                                   meetingName: String?
    ): DigitalClassroom {
        return classrooms.computeIfAbsent(meetingID) { id ->
            DigitalClassroom(
                id,
                attendeePW = attendeePW ?: RandomStringUtils.randomAlphanumeric(30),
                tutorPW = assistantPW ?: RandomStringUtils.randomAlphanumeric(30),
                moderatorPW = moderatorPW ?: RandomStringUtils.randomAlphanumeric(30),
                meetingName = meetingName ?: "Digital Classroom - ${UUID.randomUUID()}",
                internalMeetingID = "${RandomStringUtils.randomAlphanumeric(40)}-${RandomStringUtils.randomAlphanumeric(13)}"
            )
        }
    }

    fun getClassroomInstance(meetingID: String): DigitalClassroom {
        return classrooms[meetingID] ?: throw ClassroomNotFoundException(meetingID)
    }

    fun joinUser(meetingID: String, password: String, user: User): JoinRoomBBBResponse {
        val classroom = classrooms[meetingID] ?: throw ClassroomNotFoundException(meetingID)
        val joinedUser = classroom.joinUser(password, user)
        val sessionToken = RandomStringUtils.randomAlphanumeric(16)
        classroomUserDetailsRepository.insertValidToken(sessionToken, joinedUser)
        val url = URL("${classroomProperties.serviceUrl}:${serverProperties.port}/classroom?sessionToken=$sessionToken").toString()
        return JoinRoomBBBResponse(
            success = true,
            meetingID = classroom.internalMeetingID,
            sessionToken = sessionToken,
            url = url,
            userID = user.userId
        )
    }

    fun isRunning(meetingID: String) = classrooms.containsKey(meetingID)

}