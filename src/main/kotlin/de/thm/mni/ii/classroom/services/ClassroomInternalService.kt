package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.model.User
import org.springframework.stereotype.Service
import org.springframework.util.MultiValueMap
import reactor.core.publisher.Mono

@Service
class ClassroomInternalService(private val classroomInstanceManagingService: ClassroomInstanceManagingService) {

    fun openClassroom(user: User): Mono<String> = Mono.create {
        val classroom = classroomInstanceManagingService.getClassroomInstance(user.classroomId)
        it.success("Hallo ${user.fullName}. Deine Rolle: ${user.userRole.name}\n" +
                " Meeting: ${classroom.meetingName}\n" +
                " ${classroom.meetingID}")
    }

}