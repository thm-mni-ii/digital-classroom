package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.downstream.model.CreateRoomBBB
import de.thm.mni.ii.classroom.downstream.model.IsMeetingRunningBBB
import de.thm.mni.ii.classroom.downstream.model.ReturnCodeBBB
import de.thm.mni.ii.classroom.exception.MissingMeetingIDException
import de.thm.mni.ii.classroom.security.exception.NoPasswordSpecifiedException
import de.thm.mni.ii.classroom.security.exception.NoUsernameSpecifiedException
import de.thm.mni.ii.classroom.model.User
import de.thm.mni.ii.classroom.model.UserRole
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap
import reactor.core.publisher.Mono
import java.util.*
import de.thm.mni.ii.classroom.downstream.APIQueryParamTranslation.*
import de.thm.mni.ii.classroom.downstream.model.JoinRoomBBBResponse

@Component
class ClassroomApiService(private val classroomInstanceManagingService: ClassroomInstanceManagingService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun createClassroom(param: MultiValueMap<String, String>): Mono<ReturnCodeBBB> {
        return Mono.create {
            val classroomId = getClassroomId(param)
            val classroomInstance = classroomInstanceManagingService.createNewClassroomInstance(
                classroomId,
                param.getFirst(StudentPassword.api),
                param.getFirst(TutorPassword.api),
                param.getFirst(TeacherPassword.api),
                param.getFirst(ClassroomName.api)
            )
            it.success(CreateRoomBBB(classroomInstance))
        }
    }

    fun joinClassroom(param: MultiValueMap<String, String>): Mono<JoinRoomBBBResponse> {
        val classroomId = getClassroomId(param)
        val password: String = param.getFirst(Password.api) ?: throw NoPasswordSpecifiedException()
        val userId = param.getFirst(UserId.api) ?: UUID.randomUUID().toString()
        val userName = param.getFirst(userName.api) ?: throw NoUsernameSpecifiedException()
        return classroomInstanceManagingService.joinUser(
                    classroomId,
                    password,
                    User(userId, userName, classroomId, UserRole.STUDENT))
    }

    fun isMeetingRunning(param: MultiValueMap<String, String>): Mono<ReturnCodeBBB> {
        return Mono.create {
            val classroomId = getClassroomId(param)
            it.success(IsMeetingRunningBBB(classroomInstanceManagingService.isRunning(classroomId)))
        }
    }

    private fun getClassroomId(param: MultiValueMap<String, String>): String {
        val classroomId = param.getFirst(ClassroomId.api)
        if (classroomId.isNullOrEmpty()) {
            throw MissingMeetingIDException()
        }
        return classroomId
    }
}
