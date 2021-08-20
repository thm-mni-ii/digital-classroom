package de.thm.mni.ii.classroom.controller

import de.thm.mni.ii.classroom.model.classroom.Ticket
import de.thm.mni.ii.classroom.security.classroom.ClassroomAuthentication
import de.thm.mni.ii.classroom.services.ClassroomUserService
import org.slf4j.LoggerFactory
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/classroom-api")
@CrossOrigin
class ClassroomApiController(private val classroomUserService: ClassroomUserService) {

    val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Does not return any value. This route is called with sessionToken, which is exchanged to a JWT
     * within Spring Security context configured in SecurityConfiguration and SessionTokenSecurity
     * @return
     */
    @GetMapping("/join")
    fun joinClassroom(auth: ClassroomAuthentication) = Mono.empty<String>().doOnNext {
        logger.info("${auth.principal.fullName} joined classroom ${auth.principal.classroomId}.")
    }

    @GetMapping("/ticket")
    fun getTickets(auth: ClassroomAuthentication) = classroomUserService.getTickets(auth)

    @PostMapping("/ticket")
    fun createTicket(auth: ClassroomAuthentication, @RequestBody ticket: Ticket) =
        classroomUserService.createTicket(auth, ticket)

    @PutMapping("/ticket")
    @Secured("TUTOR", "TEACHER")
    fun assignTicket(auth: ClassroomAuthentication, @RequestBody ticket: Ticket) =
        classroomUserService.assignTicket(auth, ticket)

    @PostMapping("/ticket/delete")
    fun deleteTicket(auth: ClassroomAuthentication, @RequestBody ticket: Ticket) =
        classroomUserService.deleteTicket(auth, ticket)

    @GetMapping("/users")
    fun getUsers(auth: ClassroomAuthentication) = classroomUserService.getUsers(auth)

}
