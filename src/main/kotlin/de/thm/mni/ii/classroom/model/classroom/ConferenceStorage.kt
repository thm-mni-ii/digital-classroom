package de.thm.mni.ii.classroom.model.classroom

import de.thm.mni.ii.classroom.exception.classroom.ConferenceNotFoundException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

class ConferenceStorage {

    private val usersConference = HashMap<UserCredentials, LinkedHashSet<Conference>>()
    private val conferences = HashMap<String, Conference>()

    fun getConferencesOfUser(userCredentials: UserCredentials) = usersConference[userCredentials] ?: LinkedHashSet()

    fun getUsersOfConference(conference: Conference): Set<UserCredentials> {
        return conferences[conference.conferenceId]?.attendees
            ?: throw ConferenceNotFoundException(conference.conferenceId)
    }

    fun joinUser(conference: Conference, userCredentials: UserCredentials): Mono<UserCredentials> {
        return Mono.just(userCredentials).doOnNext {
            usersConference.computeIfAbsent(userCredentials) { LinkedHashSet() }
                .also { it.add(conference) }
            conferences.computeIfAbsent(conference.conferenceId) { conference }
                .also { it.attendees.add(userCredentials) }
        }
    }

    fun createConference(conference: Conference): Mono<Conference> {
        return conferences.computeIfAbsent(conference.conferenceId) { conference }.toMono()
    }

    fun getConferences(): Flux<Conference> {
        return Flux.fromIterable(conferences.values)
    }

    fun getUsersInConferences(): Flux<UserCredentials> {
        return Flux.fromIterable(usersConference.keys)
    }

    fun isUserInConference(userCredentials: UserCredentials): Mono<Boolean> {
        return Mono.just(usersConference.containsKey(userCredentials))
    }

    fun getConference(conferenceId: String): Conference? {
        return conferences[conferenceId]
    }

    fun leaveConference(userCredentials: UserCredentials, conference: Conference): Conference {
        this.conferences[conference.conferenceId]!!.attendees.remove(userCredentials)
        this.usersConference[userCredentials]?.remove(conference)
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
