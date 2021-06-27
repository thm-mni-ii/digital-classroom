package de.thm.mni.ii.classroom.controller

import de.thm.mni.ii.classroom.services.ClassroomInternalService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/classroom")
class ClassroomStaticController(private val classroomInternalService: ClassroomInternalService) {

    @GetMapping("")
    fun openClassroom(@RequestParam sessionToken: String) = classroomInternalService.openClassroom(sessionToken)

}