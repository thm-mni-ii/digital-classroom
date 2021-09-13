package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.exception.api.MissingMeetingIDException
import de.thm.mni.ii.classroom.model.api.*
import de.thm.mni.ii.classroom.exception.api.NoPasswordSpecifiedException
import de.thm.mni.ii.classroom.exception.api.NoUsernameSpecifiedException
import de.thm.mni.ii.classroom.model.classroom.User
import de.thm.mni.ii.classroom.model.classroom.UserRole
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap
import reactor.core.publisher.Mono
import java.util.*
import de.thm.mni.ii.classroom.util.APIQueryParamTranslation.*
import de.thm.mni.ii.classroom.properties.ClassroomProperties
import de.thm.mni.ii.classroom.security.classroom.ClassroomUserDetailsRepository
import org.apache.commons.lang3.RandomStringUtils
import reactor.kotlin.core.publisher.onErrorResume
import java.net.URL

@Component
class DownstreamApiService(private val classroomInstanceService: ClassroomInstanceService,
                           private val classroomUserDetailsRepository: ClassroomUserDetailsRepository,
                           private val classroomProperties: ClassroomProperties,
) {

    private val logger = LoggerFactory.getLogger(DownstreamApiService::class.java)

    fun createClassroom(param: MultiValueMap<String, String>): Mono<CreateRoomBBB> {
        val classroomId = getClassroomId(param)
        return classroomInstanceService.getClassroomInstance(classroomId)
            .onErrorResume {
                classroomInstanceService.createNewClassroomInstance(
                    classroomId = classroomId,
                    classroomName = param.getFirst(ClassroomName.api),
                    studentPassword = param.getFirst(StudentPassword.api),
                    tutorPassword = param.getFirst(TutorPassword.api),
                    teacherPassword = param.getFirst(TeacherPassword.api)
                )
            }.doOnNext {
                logger.info("Classroom ${it.classroomName} created!")
            }.map(::CreateRoomBBB)

    }

    fun joinClassroom(param: MultiValueMap<String, String>): Mono<JoinRoomBBBResponse> {
        val classroomId = getClassroomId(param)
        return Mono.defer {
            val password: String = param.getFirst(Password.api) ?: error(NoPasswordSpecifiedException())
            val userId = param.getFirst(UserId.api) ?: UUID.randomUUID().toString()
            val fullName = param.getFirst(userName.api) ?: error(NoUsernameSpecifiedException())
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

    fun isMeetingRunning(param: MultiValueMap<String, String>): Mono<IsMeetingRunningBBB> {
        return classroomInstanceService.isRunning(getClassroomId(param))
            .map(::IsMeetingRunningBBB)
    }

    private fun getClassroomId(param: MultiValueMap<String, String>): String {
        return param.getFirst(ClassroomId.api) ?: throw MissingMeetingIDException()
    }

    fun getMeetingInfo(param: MultiValueMap<String, String>): Mono<MeetingInfoBBBResponse> {
        val classroomId = param.getFirst(ClassroomId.api) ?: throw MissingMeetingIDException()
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
        val classroomId = param.getFirst(ClassroomId.api) ?: throw MissingMeetingIDException()
        val password = param.getFirst(Password.api) ?: throw NoPasswordSpecifiedException()
        return classroomInstanceService
            .endClassroom(classroomId, password)
            .thenReturn(MessageBBB(true, "sentEndMeetingRequest", "A request to end the meeting was sent. Please wait a few seconds, and then use the getMeetingInfo or isMeetingRunning API calls to verify that it was ended"))
    }

}
