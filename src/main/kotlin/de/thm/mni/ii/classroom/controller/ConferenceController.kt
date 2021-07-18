package de.thm.mni.ii.classroom.controller

import de.thm.mni.ii.classroom.model.Conference
import de.thm.mni.ii.classroom.model.User
import de.thm.mni.ii.classroom.security.classroom.ClassroomAuthentication
import de.thm.mni.ii.classroom.services.ConferenceService
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/classroom-api")
class ConferenceController(private val conferenceService: ConferenceService) {

    @GetMapping("/conference")
    fun getConferences(auth: ClassroomAuthentication): Flux<Conference> {
        return conferenceService.getConferencesOfClassroom(auth)
    }

    @GetMapping("/conference/user")
    fun getUsersInConference(auth: ClassroomAuthentication): Flux<User> {
        return conferenceService.getUsersInConferences(auth)
    }

    @GetMapping("/conference/create")
    fun createConference(auth: ClassroomAuthentication): Mono<Conference> {
        return conferenceService.createConference(auth)
    }

    @PostMapping("/conference/join")
    fun joinConference(auth: ClassroomAuthentication, @RequestBody conference: Conference): Mono<String> {
        return conferenceService.joinUser(auth, conference)
    }

    @PostMapping("/conference/join/user")
    fun joinConferenceOfUser(auth: ClassroomAuthentication, @RequestBody user: User): Mono<String> {
        return conferenceService.joinConferenceOfUser(auth, user)
    }

}