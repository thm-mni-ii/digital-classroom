package de.thm.mni.ii.classroom.controller

import de.thm.mni.ii.classroom.security.classroom.ClassroomAuthentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/classroom-api")
@CrossOrigin
class ClassroomApiController {

    @GetMapping("/join")
    fun joinClassroom(auth: ClassroomAuthentication) =
        "Successfully joined ${auth.principal.fullName} to ${auth.principal.classroomId}!"

}
