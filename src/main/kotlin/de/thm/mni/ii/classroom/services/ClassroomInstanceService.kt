package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.exception.api.ClassroomNotFoundException
import de.thm.mni.ii.classroom.exception.api.NoPasswordSpecifiedException
import de.thm.mni.ii.classroom.model.classroom.DigitalClassroom
import de.thm.mni.ii.classroom.model.classroom.User
import de.thm.mni.ii.classroom.model.classroom.UserCredentials
import de.thm.mni.ii.classroom.util.toPair
import org.apache.commons.lang3.RandomStringUtils
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.util.function.Tuple2
import java.net.URL
import java.time.Duration
import java.util.UUID
import kotlin.collections.HashMap

/**
 * Central service for managing and creating all DigitalClassroomInstances.
 */
@Service
class ClassroomInstanceService(@Lazy private val conferenceService: ConferenceService) {

    private val logger = LoggerFactory.getLogger(ClassroomInstanceService::class.java)

    private val classrooms = HashMap<String, DigitalClassroom>()
    private val scheduledDeletions = HashMap<String, Disposable>()

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
    fun createNewClassroomInstance(
        classroomId: String,
        classroomName: String?,
        studentPassword: String?,
        tutorPassword: String?,
        teacherPassword: String?,
        logoutUrl: String?
    ): Mono<DigitalClassroom> {
        return Mono.defer {
            val classroom = DigitalClassroom(
                classroomId,
                studentPassword = studentPassword ?: RandomStringUtils.randomAlphanumeric(30),
                tutorPassword = tutorPassword ?: RandomStringUtils.randomAlphanumeric(30),
                teacherPassword = teacherPassword ?: RandomStringUtils.randomAlphanumeric(30),
                classroomName = classroomName ?: "Digital Classroom - ${UUID.randomUUID()}",
                logoutUrl = logoutUrl?.let { URL(it) }
            )
            classrooms.computeIfAbsent(classroomId) { classroom }
            Mono.just(classroom)
        }
    }

    fun getClassroomInstanceSync(classroomId: String): DigitalClassroom {
        return classrooms[classroomId] ?: throw ClassroomNotFoundException(classroomId)
    }

    fun getClassroomInstance(classroomId: String): Mono<DigitalClassroom> {
        return Mono.justOrEmpty(classrooms[classroomId]).switchIfEmpty(Mono.error(ClassroomNotFoundException(classroomId)))
    }

    fun joinUser(classroomId: String, password: String, userCredentials: UserCredentials, avatarUrl: String?): Mono<Pair<User, DigitalClassroom>> {
        return getClassroomInstance(classroomId)
            .flatMap { classroom ->
                Mono.zip(classroom.authenticateAssignRole(password, userCredentials), Mono.just(classroom))
            }.map(Tuple2<UserCredentials, DigitalClassroom>::toPair)
            .map { (user, classroom) ->
                Pair(User(user, true, avatarUrl), classroom)
            }
    }

    fun isRunning(classroomId: String) = Mono.just(classrooms.containsKey(classroomId))

    fun getAllClassrooms(): Flux<DigitalClassroom> {
        return classrooms.values.toFlux()
    }

    fun endClassroom(classroomId: String, password: String): Mono<Void> {
        val classroom = classrooms[classroomId] ?: return Mono.error(ClassroomNotFoundException(classroomId))
        if (classroom.teacherPassword != password) return Mono.error(NoPasswordSpecifiedException())
        return classroom.getSockets()
            .doOnNext { (_, socket) ->
                socket?.rsocket()?.dispose()
            }.doOnComplete {
                classrooms.remove(classroomId)
            }.then()
    }

    fun scheduleClassroomDeletion(digitalClassroom: DigitalClassroom, deleteInMinutes: Long = 10L) {
        logger.debug("Classroom ${digitalClassroom.classroomName} scheduled for deletion if still empty in $deleteInMinutes minutes!")
        val disposable = Mono.just(digitalClassroom)
            .delayElement(Duration.ofMinutes(deleteInMinutes))
            // Stop if users rejoined the conference!
            .filter { classroom ->
                val updatedClassroom = this.classrooms[digitalClassroom.classroomId]
                if (updatedClassroom == null) {
                    logger.warn("Classroom scheduled for deletion is already deleted!")
                    false
                } else {
                    val usersJoined = updatedClassroom.hasUserJoined()
                    if (usersJoined) logger.debug("Users joined classroom ${updatedClassroom.classroomName}. Abort deletion.")
                    else logger.debug("Classroom ${updatedClassroom.classroomName} is still empty. Deleting...")
                    !usersJoined
                }
            }.flatMap {
                this.endClassroom(digitalClassroom.classroomId, digitalClassroom.teacherPassword)
            }.subscribe()
        this.scheduledDeletions[digitalClassroom.classroomId] = disposable
    }

    @Scheduled(fixedDelay = 60000)
    private fun updateConferences() {
        if (this.classrooms.values.isEmpty()) return
        this.classrooms.values.forEach(this.conferenceService::updateConferences)
    }

    fun abortDeletion(digitalClassroom: DigitalClassroom) {
        val disposable = this.scheduledDeletions[digitalClassroom.classroomId]
        if (disposable != null) {
            disposable.dispose()
            logger.debug("Abort deletion of ${digitalClassroom.classroomName}")
        }
    }
}
