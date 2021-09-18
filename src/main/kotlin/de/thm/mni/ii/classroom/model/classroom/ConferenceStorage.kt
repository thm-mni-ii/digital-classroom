package de.thm.mni.ii.classroom.model.classroom

import de.thm.mni.ii.classroom.exception.classroom.ConferenceNotFoundException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.empty
import reactor.kotlin.core.publisher.toMono

class ConferenceStorage {

    private val usersConference = HashMap<User, LinkedHashSet<Conference>>()
    private val conferenceUsers = HashMap<Conference, HashSet<User>>()

    fun getConferencesOfUser(user: User) = usersConference[user] ?: LinkedHashSet()

    fun getUsersOfConference(conference: Conference): Set<User> =
        conferenceUsers[conference] ?: throw ConferenceNotFoundException(conference.conferenceId)

    fun joinUser(conference: Conference, user: User): Mono<User> {
        return Mono.just(user).doOnNext {
            usersConference.computeIfAbsent(user) { LinkedHashSet() }
                .also { it.add(conference) }
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

    private fun getLatestConferenceOfUser(user: User): Conference {
        return getConferencesOfUser(user).last()
    }

    fun leaveConference(user: User, conference: Conference) {
        this.conferenceUsers[conference]!!.remove(user)
        this.usersConference[user]?.remove(conference)
    }

    fun deleteConference(conference: Conference): Mono<Void> {
        this.conferenceUsers.remove(conference)
        this.usersConference.values.forEach {
            it.remove(conference)
        }
        return empty()
    }

}
