package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.event.ClassroomEventPublisher
import de.thm.mni.ii.classroom.exception.api.ClassroomNotFoundException
import de.thm.mni.ii.classroom.model.classroom.DigitalClassroom
import de.thm.mni.ii.classroom.model.classroom.User
import de.thm.mni.ii.classroom.util.toPair
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.*
import kotlin.collections.HashMap

/**
 * Central service for managing and creating all DigitalClassroomInstances.
 */
@Service
class ClassroomInstanceService {

    private val classrooms = HashMap<String, Pair<DigitalClassroom, ClassroomEventPublisher>>()

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
    ): Mono<DigitalClassroom> {
        return Mono.defer {
            val classroom = DigitalClassroom(
                classroomId,
                studentPassword = studentPassword ?: RandomStringUtils.randomAlphanumeric(30),
                tutorPassword = tutorPassword ?: RandomStringUtils.randomAlphanumeric(30),
                teacherPassword = teacherPassword ?: RandomStringUtils.randomAlphanumeric(30),
                classroomName = classroomName ?: "Digital Classroom - ${UUID.randomUUID()}",
                internalClassroomId = "${RandomStringUtils.randomAlphanumeric(40)}-${
                    RandomStringUtils.randomAlphanumeric(
                        13
                    )
                }"
            )
            val publisher = ClassroomEventPublisher(classroom)
            classrooms.computeIfAbsent(classroomId) {
                Pair(classroom, publisher)
            }
            Mono.just(classroom)
        }
    }

    fun getClassroomInstance(classroomId: String): Mono<DigitalClassroom> {
        return getClassroomInstanceAndPublisher(classroomId).map(Pair<DigitalClassroom, Any>::first)
    }

    fun getClassroomPublisher(classroomId: String): Mono<ClassroomEventPublisher> {
        return getClassroomInstanceAndPublisher(classroomId).map(Pair<Any, ClassroomEventPublisher>::second)
    }

    private fun getClassroomInstanceAndPublisher(classroomId: String): Mono<Pair<DigitalClassroom, ClassroomEventPublisher>> {
        return Mono.justOrEmpty(classrooms[classroomId])
    }

    fun joinUser(classroomId: String, password: String, user: User): Mono<Pair<User, DigitalClassroom>> {
        return getClassroomInstance(classroomId).flatMap { classroom ->
            Mono.zip(classroom.joinUser(password, user), Mono.just(classroom)).map { it.toPair() }
        }
    }

    fun isRunning(classroomId: String) = Mono.just(classrooms.containsKey(classroomId))

}
