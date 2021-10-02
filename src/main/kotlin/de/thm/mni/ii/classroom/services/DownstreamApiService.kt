package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.exception.api.MissingMeetingIDException
import de.thm.mni.ii.classroom.exception.api.NoPasswordSpecifiedException
import de.thm.mni.ii.classroom.exception.api.NoUsernameSpecifiedException
import de.thm.mni.ii.classroom.model.api.CreateRoomBBB
import de.thm.mni.ii.classroom.model.api.GetMeetingsBBBResponse
import de.thm.mni.ii.classroom.model.api.IsMeetingRunningBBB
import de.thm.mni.ii.classroom.model.api.JoinRoomBBBResponse
import de.thm.mni.ii.classroom.model.api.MeetingInfoBBBResponse
import de.thm.mni.ii.classroom.model.api.MessageBBB
import de.thm.mni.ii.classroom.model.classroom.User
import de.thm.mni.ii.classroom.model.classroom.UserRole
import de.thm.mni.ii.classroom.properties.ClassroomProperties
import de.thm.mni.ii.classroom.security.jwt.ClassroomTokenRepository
import de.thm.mni.ii.classroom.util.BbbApiQueryParamKeys
import org.apache.commons.lang3.RandomStringUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap
import reactor.core.publisher.Mono
import java.net.URL
import java.util.UUID

@Component
class DownstreamApiService(
    private val classroomInstanceService: ClassroomInstanceService,
    private val classroomTokenRepository: ClassroomTokenRepository,
    private val classroomProperties: ClassroomProperties,
) {

    private val logger = LoggerFactory.getLogger(DownstreamApiService::class.java)

    fun createClassroom(param: MultiValueMap<String, String>): Mono<CreateRoomBBB> {
        val classroomId = getClassroomId(param)
        return classroomInstanceService.getClassroomInstance(classroomId)
            .onErrorResume {
                classroomInstanceService.createNewClassroomInstance(
                    classroomId = classroomId,
                    classroomName = param.getFirst(BbbApiQueryParamKeys.ClassroomName.key),
                    studentPassword = param.getFirst(BbbApiQueryParamKeys.StudentPassword.key),
                    tutorPassword = param.getFirst(BbbApiQueryParamKeys.TutorPassword.key),
                    teacherPassword = param.getFirst(BbbApiQueryParamKeys.TeacherPassword.key),
                    logoutUrl = param.getFirst(BbbApiQueryParamKeys.LogoutUrl.key)
                )
            }.doOnNext {
                logger.info("Classroom ${it.classroomName} created!")
            }.map(::CreateRoomBBB)
    }

    fun joinClassroom(param: MultiValueMap<String, String>): Mono<JoinRoomBBBResponse> {
        val classroomId = getClassroomId(param)
        return Mono.defer {
            val password: String = param.getFirst(BbbApiQueryParamKeys.Password.key) ?: error(NoPasswordSpecifiedException())
            val userId = param.getFirst(BbbApiQueryParamKeys.UserId.key) ?: UUID.randomUUID().toString()
            val fullName = param.getFirst(BbbApiQueryParamKeys.Username.key) ?: error(NoUsernameSpecifiedException())
            val user = User(classroomId, userId, fullName, UserRole.STUDENT)
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
                        meetingID = classroom.classroomId,
                        sessionToken = sessionToken,
                        url = URL(
                            "${classroomProperties.host}${classroomProperties.prefixPath}" +
                                "/classroom/join?sessionToken=$sessionToken"
                        ).toString(),
                        userID = user.userId
                    )
                }
        }
    }

    private fun createSessionToken(user: User): Mono<String> {
        return Mono.just(RandomStringUtils.randomAlphanumeric(16))
            .doOnNext { sessionToken ->
                classroomTokenRepository.insertSessionToken(sessionToken, user)
            }
    }

    fun isMeetingRunning(param: MultiValueMap<String, String>): Mono<IsMeetingRunningBBB> {
        return classroomInstanceService.isRunning(getClassroomId(param))
            .map(::IsMeetingRunningBBB)
    }

    private fun getClassroomId(param: MultiValueMap<String, String>): String {
        return param.getFirst(BbbApiQueryParamKeys.ClassroomId.key) ?: throw MissingMeetingIDException()
    }

    fun getMeetingInfo(param: MultiValueMap<String, String>): Mono<MeetingInfoBBBResponse> {
        val classroomId = param.getFirst(BbbApiQueryParamKeys.ClassroomId.key) ?: throw MissingMeetingIDException()
        return classroomInstanceService
            .getClassroomInstance(classroomId)
            .map { classroom ->
                MeetingInfoBBBResponse(classroom)
            }
    }

    fun getMeetings(param: MultiValueMap<String, String>): Mono<GetMeetingsBBBResponse> {
        return classroomInstanceService
            .getAllClassrooms()
            .collectList()
            .map { classroomList ->
                GetMeetingsBBBResponse(classroomList)
            }
    }

    fun end(param: MultiValueMap<String, String>): Mono<MessageBBB> {
        val classroomId = param.getFirst(BbbApiQueryParamKeys.ClassroomId.key) ?: throw MissingMeetingIDException()
        val password = param.getFirst(BbbApiQueryParamKeys.Password.key) ?: throw NoPasswordSpecifiedException()
        return classroomInstanceService
            .endClassroom(classroomId, password)
            .thenReturn(MessageBBB(true, "sentEndMeetingRequest", "A request to end the meeting was sent. Please wait a few seconds, and then use the getMeetingInfo or isMeetingRunning API calls to verify that it was ended"))
    }
}
