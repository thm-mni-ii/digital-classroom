package de.thm.mni.ii.classroom.socket

import de.thm.mni.ii.classroom.model.classroom.User
import de.thm.mni.ii.classroom.services.ClassroomInstanceService
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink

@Service
class UserSocketService(private val classroomInstanceService: ClassroomInstanceService) {

    private val sinks: MutableMap<User, FluxSink<User>> = mutableMapOf()

    fun connect(user: User): Flux<User> = Flux.create {
        classroomInstanceService.getClassroomInstance(user.classroomId).getUsers().subscribe(it::next).dispose()
        sinks[user] = it
    }

    fun disconnect(user: User) {
        sinks.remove(user)?.complete()
        sinks.forEach {

        }
    }

}
