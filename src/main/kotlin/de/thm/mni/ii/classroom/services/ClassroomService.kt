package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.model.dto.CreateRoomResponse
import de.thm.mni.ii.classroom.model.dto.JoinRoomResponse
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Component
class ClassroomService {

    fun createClassroom(param: MultiValueMap<String, String>): Mono<CreateRoomResponse> = CreateRoomResponse().toMono()

    fun joinClassroom(param: MultiValueMap<String, String>): Mono<JoinRoomResponse> = JoinRoomResponse().toMono()

}
