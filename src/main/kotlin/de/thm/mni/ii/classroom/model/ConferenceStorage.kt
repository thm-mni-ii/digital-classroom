package de.thm.mni.ii.classroom.model

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

class ConferenceStorage(private val digitalClassroom: DigitalClassroom) {

    private val usersConference = HashMap<User, Conference>()
    private val conferenceUsers = HashMap<Conference, HashSet<User>>()

    fun getConferenceOfUser(user: User) = Mono.justOrEmpty(usersConference[user])

    fun getUsersOfConference(conference: Conference) = Mono.justOrEmpty(conferenceUsers[conference])

    fun joinUser(conference: Conference, user: User): Flux<User> {
        usersConference[user] = conference
        return Flux.fromIterable(
            conferenceUsers
                .computeIfAbsent(conference) { HashSet() }
                .also { it.add(user) }
        )
    }

    fun createConference(conference: Conference): Mono<Conference> {
        conferenceUsers.computeIfAbsent(conference) { HashSet() }
        return conference.toMono()
    }

    fun getConferences(): Flux<Conference> {
        return Flux.fromIterable(conferenceUsers.keys)
    }

}