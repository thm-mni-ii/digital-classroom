package de.thm.mni.ii.classroom.controller

import de.thm.mni.ii.classroom.security.jwt.ClassroomAuthentication
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
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
    fun joinClassroom(auth: ClassroomAuthentication, exchange: ServerWebExchange): Mono<ServerHttpResponse> {
        return Mono.just(
                setAuthHeader(auth.credentials!!, exchange).response
            ).doOnNext {
                logger.info("${auth.principal.fullName} joined classroom ${auth.principal.classroomId}.")
            }
    }

    @GetMapping("/refresh")
    fun refreshToken(auth: ClassroomAuthentication) = Mono.empty<String>().doOnNext {
        logger.info("${auth.principal.fullName} refreshed his token!")
    }

    private fun setAuthHeader(authToken: String, exchange: ServerWebExchange): ServerWebExchange {
        exchange.response
            .headers
            .add(HttpHeaders.AUTHORIZATION, "Bearer $authToken")
        return exchange
    }
}
