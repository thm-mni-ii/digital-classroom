package de.thm.mni.ii.classroom.controller

import de.thm.mni.ii.classroom.exception.MissingMeetingIDException
import de.thm.mni.ii.classroom.model.dto.*
import de.thm.mni.ii.classroom.services.ClassroomService
import org.slf4j.LoggerFactory
import org.springframework.util.MimeTypeUtils
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import kotlin.Exception

@RestController
@RequestMapping("/api", produces = [MimeTypeUtils.APPLICATION_XML_VALUE])
class BBBApiController(private val classroomService: ClassroomService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/create")
    fun createClassroomInstance(@RequestParam param: MultiValueMap<String, String>): Mono<ReturnCodeBBB> =
        classroomService.createClassroom(param).onErrorResume {
            errorHandler(it)
        }

    @GetMapping("/join")
    fun joinUserToClassroom(@RequestParam param: MultiValueMap<String, String>): Mono<ReturnCodeBBB> =
        classroomService.joinClassroom(param).onErrorResume {
            errorHandler(it)
        }

    @GetMapping("/isMeetingRunning")
    fun isMeetingRunning(@RequestParam param: MultiValueMap<String, String>): Mono<ReturnCodeBBB> = classroomService.isMeetingRunning(param)

    private fun errorHandler(throwable: Throwable): Mono<MessageBBB> {
        return Mono.create {
            if (throwable is MissingMeetingIDException) {
                it.success(MessageBBB(false, "missingParamMeetingID", "You must specify a meeting ID for the meeting."))
            } else {
                it.error(throwable)
            }
        }
    }

}
