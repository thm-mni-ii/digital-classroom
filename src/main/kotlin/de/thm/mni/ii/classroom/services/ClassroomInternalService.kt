package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.model.User
import org.springframework.stereotype.Service
import org.springframework.util.MultiValueMap
import reactor.core.publisher.Mono

@Service
class ClassroomInternalService(private val classroomInstanceManagingService: ClassroomInstanceManagingService) {

    fun openClassroom(sessionToken: String): Mono<String> = Mono.create {
        val user = getUserBySessionToken(sessionToken)
        it.success("Hallo ${user.fullName}")
    }

    fun getUserBySessionToken(sessionToken: String): User {
        return classroomInstanceManagingService.getUserBySessionToken(sessionToken)!!.second
    }

}