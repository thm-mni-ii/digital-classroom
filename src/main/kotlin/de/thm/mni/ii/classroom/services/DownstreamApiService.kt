package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.model.api.CreateRoomBBB
import de.thm.mni.ii.classroom.model.api.IsMeetingRunningBBB
import de.thm.mni.ii.classroom.model.api.ReturnCodeBBB
import de.thm.mni.ii.classroom.exception.api.MissingMeetingIDException
import de.thm.mni.ii.classroom.security.exception.NoPasswordSpecifiedException
import de.thm.mni.ii.classroom.security.exception.NoUsernameSpecifiedException
import de.thm.mni.ii.classroom.model.classroom.User
import de.thm.mni.ii.classroom.model.classroom.UserRole
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap
import reactor.core.publisher.Mono
import java.util.*
import de.thm.mni.ii.classroom.util.APIQueryParamTranslation.*
import de.thm.mni.ii.classroom.model.api.JoinRoomBBBResponse
import de.thm.mni.ii.classroom.properties.ClassroomProperties
import de.thm.mni.ii.classroom.security.classroom.ClassroomUserDetailsRepository
import org.apache.commons.lang3.RandomStringUtils
import java.net.URL

@Component
class DownstreamApiService(private val classroomInstanceService: ClassroomInstanceService,
                           private val classroomUserDetailsRepository: ClassroomUserDetailsRepository,
                           private val classroomProperties: ClassroomProperties,
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun createClassroom(param: MultiValueMap<String, String>): Mono<CreateRoomBBB> {
        val classroomId = getClassroomId(param)
        return classroomInstanceService.getClassroomInstance(classroomId)
            .switchIfEmpty(
                classroomInstanceService.createNewClassroomInstance(
                    classroomId,
                    param.getFirst(StudentPassword.api),
                    param.getFirst(TutorPassword.api),
                    param.getFirst(TeacherPassword.api),
                    param.getFirst(ClassroomName.api)
                )
            ).doOnNext {
                logger.info("Classroom ${it.classroomName} created!")
            }.map(::CreateRoomBBB)

    }

    fun joinClassroom(param: MultiValueMap<String, String>): Mono<JoinRoomBBBResponse> {
        val classroomId = getClassroomId(param)
        return Mono.defer {
            val password: String = param.getFirst(Password.api) ?: error(NoPasswordSpecifiedException())
            val userId = param.getFirst(UserId.api) ?: UUID.randomUUID().toString()
            val userName = param.getFirst(userName.api) ?: error(NoUsernameSpecifiedException())
            val user = User(userId, userName, classroomId, UserRole.STUDENT)
            classroomInstanceService.joinUser(classroomId, password, user)
                .flatMap { (user, classroom) ->
                    createSessionToken(user).map { sessionToken ->
                        Triple(user, classroom, sessionToken)
                    }
                }.doOnNext { (user, classroom, sessionToken) ->
                    logger.info("${user.userRole.name} ${user.fullName} registered in classroom ${classroom.classroomName} with token $sessionToken!")
                }.map { (user, classroom, sessionToken) ->
                    JoinRoomBBBResponse(
                        success = true,
                        meetingID = classroom.internalClassroomId,
                        sessionToken = sessionToken,
                        url = URL("${classroomProperties.host}${classroomProperties.prefixPath}" +
                                "/classroom/join?sessionToken=$sessionToken").toString(),
                        userID = user.userId
                    )
                }
            }
    }

    private fun createSessionToken(user: User): Mono<String> {
        return Mono.just(RandomStringUtils.randomAlphanumeric(16))
            .doOnNext { sessionToken ->
                classroomUserDetailsRepository.insertValidToken(sessionToken, user)
            }
    }

    fun isMeetingRunning(param: MultiValueMap<String, String>): Mono<ReturnCodeBBB> {
        return classroomInstanceService.isRunning(getClassroomId(param))
            .map(::IsMeetingRunningBBB)
    }

    private fun getClassroomId(param: MultiValueMap<String, String>): String {
        return param.getFirst(ClassroomId.api) ?: throw MissingMeetingIDException()
    }

}
