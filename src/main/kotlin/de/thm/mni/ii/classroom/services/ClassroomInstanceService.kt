package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.exception.api.ClassroomNotFoundException
import de.thm.mni.ii.classroom.exception.api.NoPasswordSpecifiedException
import de.thm.mni.ii.classroom.model.classroom.DigitalClassroom
import de.thm.mni.ii.classroom.model.classroom.User
import de.thm.mni.ii.classroom.util.toPair
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import java.util.UUID
import kotlin.collections.HashMap

/**
 * Central service for managing and creating all DigitalClassroomInstances.
 */
@Service
class ClassroomInstanceService(private val senderService: ClassroomEventSenderService) {

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
    fun createNewClassroomInstance(
        classroomId: String,
        classroomName: String?,
        studentPassword: String?,
        tutorPassword: String?,
        teacherPassword: String?
    ): Mono<DigitalClassroom> {
        return Mono.defer {
            val classroom = DigitalClassroom(
                classroomId,
                studentPassword = studentPassword ?: RandomStringUtils.randomAlphanumeric(30),
                tutorPassword = tutorPassword ?: RandomStringUtils.randomAlphanumeric(30),
                teacherPassword = teacherPassword ?: RandomStringUtils.randomAlphanumeric(30),
                classroomName = classroomName ?: "Digital Classroom - ${UUID.randomUUID()}"
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

    fun joinUser(classroomId: String, password: String, user: User): Mono<Pair<User, DigitalClassroom>> {
        return getClassroomInstance(classroomId).flatMap { classroom ->
            Mono.zip(classroom.authenticateAssignRole(password, user), Mono.just(classroom)).map { it.toPair() }
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
}
