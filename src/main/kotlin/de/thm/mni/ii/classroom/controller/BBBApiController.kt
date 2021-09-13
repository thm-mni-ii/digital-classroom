package de.thm.mni.ii.classroom.controller

import de.thm.mni.ii.classroom.exception.api.ApiException
import de.thm.mni.ii.classroom.model.api.*
import de.thm.mni.ii.classroom.services.DownstreamApiService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.MimeTypeUtils
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.RouterFunctions.route
import org.springframework.web.reactive.result.view.RedirectView
import org.springframework.web.reactive.result.view.View
import reactor.core.publisher.Mono
import java.lang.Exception
import java.net.URI

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

    private val logger = LoggerFactory.getLogger(BBBApiController::class.java)

    /**
     * Route called to create a new classroom instance.
     * @param params Request query parameters as MultiValueMap containing a meetingId / classroomId.
     * @return Mono producing a BBB-like answer in XML format containing an error or information about the classroom.
     * @see CreateRoomBBB
     */
    @RequestMapping("/create", method = [RequestMethod.GET, RequestMethod.POST], produces = [MimeTypeUtils.APPLICATION_XML_VALUE])
    fun createClassroomInstance(@RequestParam params: MultiValueMap<String, String>): Mono<CreateRoomBBB>
        = downStreamApiService.createClassroom(params)


    /**
     * Route called to join an existing classroom instance. If query param "redirect" is set to true: redirects to joinURL.
     * @return Mono producing a BBB-like answer in XML format containing an error or the url to join the classroom.
     * @see JoinRoomBBBResponse
     */
    @Bean
    fun joinUserToClassroom(): RouterFunction<ServerResponse?> {
        return route(RequestPredicates.GET("/api/join")) { req ->
            downStreamApiService.joinClassroom(req.queryParams()).flatMap { joinRoom ->
                if (req.queryParamOrNull("redirect").toBoolean()) {
                    ServerResponse.temporaryRedirect(URI(joinRoom.url)).bodyValue(joinRoom)
                } else {
                    ServerResponse.ok().contentType(MediaType.APPLICATION_XML).bodyValue(joinRoom)
                }
            }
        }
    }


    //@RequestMapping("/join", method = [RequestMethod.GET, RequestMethod.POST], produces = [MimeTypeUtils.APPLICATION_XML_VALUE])
    fun joinUserToClassroom(@RequestParam params: MultiValueMap<String, String>): Mono<Any>
        = downStreamApiService.joinClassroom(params).map {
            if (params.getFirst("redirect").toBoolean()) {
                RedirectView(it.url)
            } else {
                it
            }
        }

    @RequestMapping("/isMeetingRunning", method = [RequestMethod.GET, RequestMethod.POST], produces = [MimeTypeUtils.APPLICATION_XML_VALUE])
    fun isMeetingRunning(@RequestParam param: MultiValueMap<String, String>): Mono<ReturnCodeBBB>
        = downStreamApiService.isMeetingRunning(param)

    @RequestMapping("/getMeetingInfo", method = [RequestMethod.GET, RequestMethod.POST], produces = [MimeTypeUtils.APPLICATION_XML_VALUE])
    fun getMeetingInfo(@RequestParam param: MultiValueMap<String, String>): Mono<MeetingInfoBBBResponse>
        = downStreamApiService.getMeetingInfo(param)

    @RequestMapping("/getMeetings", method = [RequestMethod.GET, RequestMethod.POST], produces = [MimeTypeUtils.APPLICATION_XML_VALUE])
    fun getMeetings(@RequestParam param: MultiValueMap<String, String>): Mono<GetMeetingsBBBResponse>
            = downStreamApiService.getMeetings(param)

    @RequestMapping("/end", method = [RequestMethod.GET, RequestMethod.POST], produces = [MimeTypeUtils.APPLICATION_XML_VALUE])
    fun end(@RequestParam param: MultiValueMap<String, String>): Mono<MessageBBB>
            = downStreamApiService.end(param).doOnNext {
                logger.info(it.message)
    }
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
