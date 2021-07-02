package de.thm.mni.ii.classroom.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.function.server.RequestPredicates
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.net.URI

@Configuration
@CrossOrigin
class ClassroomStaticController {

    @Bean
    fun index(
        @Value("classpath:static/index.html") html: Resource
    ): RouterFunction<ServerResponse?> {
        return RouterFunctions.route(
            RequestPredicates.GET("/")
        ) { ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue(html) }
    }

    @Bean
    fun join(
        @Value("classpath:static/index.html") html: Resource
    ): RouterFunction<ServerResponse?> {
        return RouterFunctions.route(
            RequestPredicates.GET("/join")
        ) { ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue(html) }
    }
}
