package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.exception.MissingMeetingIDException
import de.thm.mni.ii.classroom.security.exception.NoPasswordSpecifiedException
import de.thm.mni.ii.classroom.security.exception.NoUsernameSpecifiedException
import de.thm.mni.ii.classroom.model.User
import de.thm.mni.ii.classroom.model.UserRole
import de.thm.mni.ii.classroom.model.dto.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap
import reactor.core.publisher.Mono
import java.util.*

@Component
class ClassroomApiService(private val classroomInstanceManagingService: ClassroomInstanceManagingService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun createClassroom(param: MultiValueMap<String, String>): Mono<ReturnCodeBBB> {
        return Mono.create {
            val meetingID = getMeetingID(param)
            val classroomInstance = classroomInstanceManagingService.createNewClassroomInstance(
                meetingID,
                param.getFirst("attendeePW"),
                param.getFirst("assistantPW"),
                param.getFirst("moderatorPW"),
                param.getFirst("meetingName")
            )
            it.success(CreateRoomBBB(classroomInstance))
        }
    }

    fun joinClassroom(param: MultiValueMap<String, String>): Mono<ReturnCodeBBB> {
        val meetingID = getMeetingID(param)
        val password: String = param.getFirst("password") ?: throw NoPasswordSpecifiedException()
        val userId = param.getFirst("userID") ?: UUID.randomUUID().toString()
        val userName = param.getFirst("fullName") ?: throw NoUsernameSpecifiedException()
        return Mono.create {
            it.success(
                classroomInstanceManagingService.joinUser(
                    meetingID,
                    password,
                    User(userId, userName, meetingID, UserRole.STUDENT)
                )
            )
        }
    }

    fun isMeetingRunning(param: MultiValueMap<String, String>): Mono<ReturnCodeBBB> {
        return Mono.create {
            val meetingID = getMeetingID(param)
            it.success(IsMeetingRunningBBB(classroomInstanceManagingService.isRunning(meetingID)))
        }
    }

    private fun getMeetingID(param: MultiValueMap<String, String>): String {
        val meetingID = param.getFirst("meetingID")
        if (meetingID.isNullOrEmpty()) {
            throw MissingMeetingIDException()
        }
        return meetingID
    }
}
