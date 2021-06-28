package de.thm.mni.ii.classroom.controller

import de.thm.mni.ii.classroom.model.User
import de.thm.mni.ii.classroom.security.classroom.ClassroomAuthentication
import de.thm.mni.ii.classroom.services.ClassroomInternalService
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/classroom")
@CrossOrigin
class ClassroomStaticController(private val classroomInternalService: ClassroomInternalService) {

    @GetMapping("")
    fun openClassroom(auth: ClassroomAuthentication) = classroomInternalService.openClassroom(auth.principal)

}