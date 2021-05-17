package de.thm.mni.ii.classroom.controller

import org.springframework.stereotype.Controller
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.function.server.ServerRequest

@Controller
@RequestMapping("/api/v1")
class ClassroomReactiveController {

    @GetMapping("/create")
    fun createClassroomInstance(request: ServerRequest, @RequestParam param: MultiValueMap<String, String>): Nothing = TODO()

    @GetMapping("/join")
    fun joinUserToClassroom(request: ServerRequest, @RequestParam param: MultiValueMap<String, String>): Nothing = TODO()

}
