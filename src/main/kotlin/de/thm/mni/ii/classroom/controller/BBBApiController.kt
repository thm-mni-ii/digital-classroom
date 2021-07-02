package de.thm.mni.ii.classroom.controller

import de.thm.mni.ii.classroom.downstream.model.JoinRoomBBBResponse
import de.thm.mni.ii.classroom.downstream.model.MessageBBB
import de.thm.mni.ii.classroom.downstream.model.ReturnCodeBBB
import de.thm.mni.ii.classroom.exception.ApiException
import de.thm.mni.ii.classroom.exception.ClassroomNotFoundException
import de.thm.mni.ii.classroom.exception.MissingMeetingIDException
import de.thm.mni.ii.classroom.services.ClassroomApiService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.util.MimeTypeUtils
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.onErrorMap
import java.lang.Exception

@RestController
@RequestMapping("/api", produces = [MimeTypeUtils.APPLICATION_XML_VALUE])
@CrossOrigin
class BBBApiController(private val classroomApiService: ClassroomApiService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/create")
    fun createClassroomInstance(@RequestParam param: MultiValueMap<String, String>): Mono<ReturnCodeBBB> =
        classroomApiService.createClassroom(param)

    @GetMapping("/join")
    fun joinUserToClassroom(@RequestParam param: MultiValueMap<String, String>): Mono<JoinRoomBBBResponse> {
        return classroomApiService.joinClassroom(param)
    }

    @GetMapping("/isMeetingRunning")
    fun isMeetingRunning(@RequestParam param: MultiValueMap<String, String>): Mono<ReturnCodeBBB> = classroomApiService.isMeetingRunning(param)

    @ExceptionHandler
    private fun errorHandler(exception: Exception): ResponseEntity<ReturnCodeBBB> {
        logger.error("", exception)
        return when (exception) {
            !is ApiException -> {
                val apiException = ApiException(cause = exception)
                ResponseEntity.badRequest().body(MessageBBB(false, apiException.bbbMessageKey, apiException.bbbMessage))
            }
            else -> ResponseEntity.badRequest().body(MessageBBB(false, exception.bbbMessageKey, exception.bbbMessage))
        }
    }
}
