package de.thm.mni.ii.classroom.controller

import de.thm.mni.ii.classroom.model.classroom.User
import de.thm.mni.ii.classroom.security.exception.UnauthorizedException
import de.thm.mni.ii.classroom.security.jwt.ClassroomAuthentication
import de.thm.mni.ii.classroom.security.jwt.ClassroomJwtService
import de.thm.mni.ii.classroom.security.jwt.ClassroomTokenRepository
import de.thm.mni.ii.classroom.util.component1
import de.thm.mni.ii.classroom.util.component2
import org.apache.commons.lang3.RandomStringUtils
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@RestController
@RequestMapping("/classroom-api")
@CrossOrigin
class ClassroomApiController(
    val classroomTokenRepository: ClassroomTokenRepository,
    val jwtService: ClassroomJwtService
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Does not return any value. This route is called with sessionToken, which is exchanged to a JWT
     * within Spring Security context configured in SecurityConfiguration and SessionTokenSecurity
     * @return
     */
    @GetMapping("/join")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun joinClassroom(auth: ClassroomAuthentication, originalExchange: ServerWebExchange): Mono<ServerHttpResponse> {
        return Mono.just(generateRefreshToken(auth.principal))
            .map { refreshToken ->
                // Set refresh_token header
                val refreshTokenSet = setHeader("refresh_token", refreshToken, originalExchange)
                // Set Authorization header
                setHeader(HttpHeaders.AUTHORIZATION, "Bearer ${auth.credentials}", refreshTokenSet).response
            }.doOnNext {
                logger.info("${auth.principal} joined classroom ${auth.principal.classroomId}.")
            }
    }

    @GetMapping("/refresh")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun refreshToken(
        auth: ClassroomAuthentication,
        originalExchange: ServerWebExchange,
        @RequestHeader("refresh_token") refreshToken: String
    ): Mono<ServerHttpResponse> {
        return classroomTokenRepository
            .findRefreshToken(refreshToken)
            .switchIfEmpty {
                val ex = UnauthorizedException("Invalid refresh token provided by user ${auth.user?.fullName}")
                logger.error("", ex)
                Mono.error(ex)
            }.filter { user ->
                user == auth.user
            }.switchIfEmpty {
                val ex = UnauthorizedException("Owner of refresh token does not match requester!")
                logger.error("", ex)
                Mono.error(ex)
            }.map { user ->
                val newRefreshToken = generateRefreshToken(user)
                Pair(user, setHeader("refresh_token", newRefreshToken, originalExchange))
            }.flatMap { (user, exchange) ->
                Mono.zip(jwtService.createToken(user), Mono.just(exchange))
            }.map { (jwt, exchange) ->
                setHeader(HttpHeaders.AUTHORIZATION, "Bearer $jwt", exchange).response
            }.doOnNext {
                logger.info("${auth.principal} refreshed his JWT!")
            }
    }

    private fun generateRefreshToken(user: User): String {
        val newRefreshToken = RandomStringUtils.randomAscii(30)
        classroomTokenRepository.insertRefreshToken(newRefreshToken, user)
        return newRefreshToken
    }

    private fun setHeader(header: String, value: String, exchange: ServerWebExchange): ServerWebExchange {
        exchange.response.headers.add(header, value)
        return exchange
    }
}
