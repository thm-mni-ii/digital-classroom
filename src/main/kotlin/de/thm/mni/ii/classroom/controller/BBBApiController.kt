package de.thm.mni.ii.classroom.controller

import de.thm.mni.ii.classroom.exception.api.ApiException
import de.thm.mni.ii.classroom.model.api.*
import de.thm.mni.ii.classroom.services.DownstreamApiService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Mono
import java.net.URI

@Configuration
class BBBApiController(private val downstreamApiService: DownstreamApiService) {

    private val logger = LoggerFactory.getLogger(BBBApiController::class.java)

    @Bean
    fun router() = router {
        "/api".nest {
            GET("/create", ::createClassroomInstance)
            POST("/create", ::createClassroomInstance)

            GET("/join", ::joinUserToClassroom)
            POST("/join", ::joinUserToClassroom)

            GET("/isMeetingRunning", ::isMeetingRunning)
            POST("/isMeetingRunning", ::isMeetingRunning)

            GET("/getMeetings", ::getMeetings)
            POST("/getMeetings", ::getMeetings)

            GET("/getMeetingInfo", ::getMeetingInfo)
            POST("/getMeetingInfo", ::getMeetingInfo)

            GET("/end", ::end)
            POST("/end", ::end)

            onError<Exception>(::errorHandler)
        }
    }

    /**
     * Route called to create a new classroom instance.
     * @return Mono producing a BBB-like answer in XML format containing an error or information about the classroom.
     * @see CreateRoomBBB
     */
    fun createClassroomInstance(req: ServerRequest): Mono<ServerResponse> =
        downstreamApiService.createClassroom(req.queryParams()).flatMap {
            ServerResponse.ok().bodyValue(it)
        }

    /**
     * Route called to join an existing classroom instance. If query param "redirect" is set to true: redirects to joinURL.
     * @return Mono producing a BBB-like answer in XML format containing an error or the url to join the classroom.
     * @see JoinRoomBBBResponse
     */
    fun joinUserToClassroom(req: ServerRequest): Mono<ServerResponse> {
        return downstreamApiService.joinClassroom(req.queryParams())
            .flatMap { joinRoom ->
                if (req.queryParamOrNull("redirect").toBoolean()) {
                    ServerResponse.temporaryRedirect(URI(joinRoom.url)).bodyValue(joinRoom)
                } else {
                    ServerResponse.ok().contentType(MediaType.APPLICATION_XML).bodyValue(joinRoom)
                }.onErrorResume(::errorHandler)
            }
    }

    fun isMeetingRunning(req: ServerRequest): Mono<ServerResponse> =
        downstreamApiService.isMeetingRunning(req.queryParams()).flatMap {
            ServerResponse.ok().bodyValue(it)
        }

    fun getMeetingInfo(req: ServerRequest): Mono<ServerResponse> =
        downstreamApiService.getMeetingInfo(req.queryParams()).flatMap {
            ServerResponse.ok().bodyValue(it)
        }

    fun getMeetings(req: ServerRequest): Mono<ServerResponse> =
        downstreamApiService.getMeetings(req.queryParams()).flatMap {
            ServerResponse.ok().bodyValue(it)
        }

    fun end(req: ServerRequest): Mono<ServerResponse> =
        downstreamApiService.end(req.queryParams()).flatMap {
            ServerResponse.ok().bodyValue(it)
        }
    /**
     * Exception handler. Any exception thrown within the class hierarchy of this controller is handled here.
     * This method constructs a BBB-API conforming error message and returns it instead of the successful response.
     * @param exception the runtime exception.
     * @return a ResponseEntity containing the error message.
     * @see ReturnCodeBBB
     */
    fun errorHandler(exception: Throwable, serverRequest: ServerRequest? = null): Mono<ServerResponse> {
        logger.error(exception.message)
        return when (exception) {
            !is ApiException -> {
                val apiException = ApiException(cause = exception)
                ServerResponse.badRequest().bodyValue(MessageBBB(false, apiException.bbbMessageKey, apiException.bbbMessage))
            }
            else -> ServerResponse.badRequest().bodyValue(MessageBBB(false, exception.bbbMessageKey, exception.bbbMessage))
        }
    }
}
