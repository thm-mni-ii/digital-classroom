package de.thm.mni.ii.classroom.controller

import de.thm.mni.ii.classroom.model.Conference
import de.thm.mni.ii.classroom.model.User
import de.thm.mni.ii.classroom.security.classroom.ClassroomAuthentication
import de.thm.mni.ii.classroom.services.ConferenceService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import reactor.core.publisher.Mono

@RequestMapping("/classroom-api")
class ConferenceController(private val conferenceService: ConferenceService) {

    @GetMapping("/conference")
    fun getConferences(auth: ClassroomAuthentication) {
        conferenceService.getConferencesOfClassroom(auth)
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