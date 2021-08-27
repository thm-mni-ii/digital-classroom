package de.thm.mni.ii.classroom.controller

import de.thm.mni.ii.classroom.security.jwt.ClassroomAuthentication
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/classroom-api")
@CrossOrigin
class ClassroomApiController {

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Does not return any value. This route is called with sessionToken, which is exchanged to a JWT
     * within Spring Security context configured in SecurityConfiguration and SessionTokenSecurity
     * @return
     */
    @GetMapping("/join")
    fun joinClassroom(auth: ClassroomAuthentication) = Mono.empty<String>().doOnNext {
        logger.info("${auth.principal.fullName} joined classroom ${auth.principal.classroomId}.")
    }

}
