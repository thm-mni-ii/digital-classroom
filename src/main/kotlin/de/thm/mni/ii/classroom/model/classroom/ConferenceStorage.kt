package de.thm.mni.ii.classroom.model.classroom

import de.thm.mni.ii.classroom.exception.classroom.ConferenceNotFoundException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

class ConferenceStorage {

    private val usersConference = HashMap<User, LinkedHashSet<Conference>>()
    private val conferences = HashMap<String, Conference>()

    fun getConferencesOfUser(user: User) = usersConference[user] ?: LinkedHashSet()

    fun getUsersOfConference(conference: Conference): Set<User> {
        return conferences[conference.conferenceId]?.attendees
            ?: throw ConferenceNotFoundException(conference.conferenceId)
    }

    fun joinUser(conference: Conference, user: User): Mono<User> {
        return Mono.just(user).doOnNext {
            usersConference.computeIfAbsent(user) { LinkedHashSet() }
                .also { it.add(conference) }
            conferences.computeIfAbsent(conference.conferenceId) { conference }
                .also { it.attendees.add(user) }
        }
    }

    fun createConference(conference: Conference): Mono<Conference> {
        return conferences.computeIfAbsent(conference.conferenceId) { conference }.toMono()
    }

    fun getConferences(): Flux<Conference> {
        return Flux.fromIterable(conferences.values)
    }

    fun getUsersInConferences(): Flux<User> {
        return Flux.fromIterable(usersConference.keys)
    }

    fun isUserInConference(user: User): Mono<Boolean> {
        return Mono.just(usersConference.containsKey(user))
    }

    fun getConference(conferenceId: String): Conference? {
        return conferences[conferenceId]
    }

    fun leaveConference(user: User, conference: Conference): Conference {
        this.conferences[conference.conferenceId]!!.attendees.remove(user)
        this.usersConference[user]?.remove(conference)
        return this.conferences[conference.conferenceId]!!
    }

    fun deleteConference(conference: Conference): Mono<Conference> {
        return Mono.justOrEmpty(conference).doOnNext {
            this.conferences.remove(conference.conferenceId)
            this.usersConference.values.forEach {
                it.remove(conference)
            }
        }
    }
}
