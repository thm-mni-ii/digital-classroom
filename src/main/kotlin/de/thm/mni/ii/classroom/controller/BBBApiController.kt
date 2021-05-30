package de.thm.mni.ii.classroom.controller

import de.thm.mni.ii.classroom.model.dto.CreateRoomResponse
import de.thm.mni.ii.classroom.model.dto.JoinRoomResponse
import de.thm.mni.ii.classroom.services.ClassroomService
import org.springframework.util.MimeTypeUtils
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1", produces = [MimeTypeUtils.APPLICATION_XML_VALUE])
class BBBApiController(private val classroomService: ClassroomService) {

    @GetMapping("/create")
    fun createClassroomInstance(@RequestParam param: MultiValueMap<String, String>): Mono<CreateRoomResponse> = classroomService.createClassroom(param)

    @GetMapping("/join")
    fun joinUserToClassroom(@RequestParam param: MultiValueMap<String, String>): Mono<JoinRoomResponse> = classroomService.joinClassroom(param)

}
