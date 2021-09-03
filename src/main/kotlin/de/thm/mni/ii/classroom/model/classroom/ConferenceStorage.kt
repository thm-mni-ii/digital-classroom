package de.thm.mni.ii.classroom.model.classroom

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

class ConferenceStorage(private val digitalClassroom: DigitalClassroom) {

    private val usersConference = HashMap<User, Conference>()
    private val conferenceUsers = HashMap<Conference, HashSet<User>>()

    fun getConferenceOfUser(user: User) = usersConference[user]

    fun getUsersOfConference(conference: Conference) = Mono.justOrEmpty(conferenceUsers[conference])

    fun joinUser(conference: Conference, user: User): Mono<User> {
        return Mono.just(user).doOnNext {
            usersConference[user] = conference
            conferenceUsers.computeIfAbsent(conference) { HashSet() }
                .also { it.add(user) }
        }
    }

    fun createConference(conference: Conference): Mono<Conference> {
        conferenceUsers.computeIfAbsent(conference) { HashSet() }
        return conference.toMono()
    }

    fun getConferences(): Flux<Conference> {
        return Flux.fromIterable(conferenceUsers.keys)
    }

    fun getUsersInConferences(): Flux<User> {
        return Flux.fromIterable(usersConference.keys)
    }

    fun isUserInConference(user: User): Mono<Boolean> {
        return Mono.just(usersConference.containsKey(user))
    }

    fun getConference(conferenceId: String): Mono<Conference> {
        return Mono.just(conferenceUsers.keys.first { it.conferenceId == conferenceId })
    }

}
