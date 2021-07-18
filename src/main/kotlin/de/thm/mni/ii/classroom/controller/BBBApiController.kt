package de.thm.mni.ii.classroom.controller

import de.thm.mni.ii.classroom.downstream.model.CreateRoomBBB
import de.thm.mni.ii.classroom.downstream.model.JoinRoomBBBResponse
import de.thm.mni.ii.classroom.downstream.model.MessageBBB
import de.thm.mni.ii.classroom.downstream.model.ReturnCodeBBB
import de.thm.mni.ii.classroom.exception.ApiException
import de.thm.mni.ii.classroom.services.DownstreamApiService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.util.MimeTypeUtils
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.lang.Exception

/**
 * Spring Controller for downstream / BBB-like API traffic.
 * All routes give an answer in BBB-API XML format.
 * @param downStreamApiService: Autowired DownstreamApiService
 * @see ReturnCodeBBB
 */
@RestController
@RequestMapping("/api", produces = [MimeTypeUtils.APPLICATION_XML_VALUE])
@CrossOrigin
class BBBApiController(private val downStreamApiService: DownstreamApiService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Route called to create a new classroom instance.
     * @param params Request query parameters as MultiValueMap containing a meetingId / classroomId.
     * @return Mono producing a BBB-like answer in XML format containing an error or information about the classroom.
     * @see CreateRoomBBB
     */
    @GetMapping("/create")
    fun createClassroomInstance(@RequestParam params: MultiValueMap<String, String>): Mono<CreateRoomBBB>
        = downStreamApiService.createClassroom(params)

    /**
     * Route called to join an existing classroom instance.
     * @param params Request query parameters as MultiValueMap containing information about the classroom, the user and
     * a password.
     * @return Mono producing a BBB-like answer in XML format containing an error or the url to join the classroom.
     * @see JoinRoomBBBResponse
     */
    @GetMapping("/join")
    fun joinUserToClassroom(@RequestParam params: MultiValueMap<String, String>): Mono<JoinRoomBBBResponse>
        = downStreamApiService.joinClassroom(params)

    @GetMapping("/isMeetingRunning")
    fun isMeetingRunning(@RequestParam param: MultiValueMap<String, String>): Mono<ReturnCodeBBB>
        = downStreamApiService.isMeetingRunning(param)

    /**
     * Controller exception handler. Any exception thrown within the class hierarchy of this controller is handled here.
     * This method constructs a BBB-API conforming error message and returns it instead of the successful response.
     * @param exception the runtime exception.
     * @return a ResponseEntity containing the error message.
     * @see ReturnCodeBBB
     */
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
