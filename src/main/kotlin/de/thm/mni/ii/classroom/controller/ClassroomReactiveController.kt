package de.thm.mni.ii.classroom.controller

import org.springframework.stereotype.Controller
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1")
class ClassroomReactiveController {

    @GetMapping("/create")
    fun createClassroomInstance(@RequestParam param: MultiValueMap<String, String>): Mono<String> = Mono.just("Hallo Welt")

    @GetMapping("/join")
    fun joinUserToClassroom(@RequestParam param: MultiValueMap<String, String>): Nothing = TODO()

}
