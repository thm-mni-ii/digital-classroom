package de.thm.mni.ii.classroom.model.classroom

import de.thm.mni.ii.classroom.exception.classroom.ConferenceNotFoundException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import java.util.concurrent.ConcurrentHashMap

class ConferenceStorage {

    private val usersConference = ConcurrentHashMap<UserCredentials, LinkedHashSet<Conference>>()
    private val conferences = ConcurrentHashMap<String, Conference>()

    fun getUsersOfConference(conference: Conference): Flux<UserCredentials> {
        return conferences[conference.conferenceId]?.attendees?.toFlux()
            ?: throw ConferenceNotFoundException(conference.conferenceId)
    }

    fun joinUser(conference: Conference, userCredentials: UserCredentials): Mono<Conference> {
        return Mono.just(userCredentials).map {
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
        return conferences.values.toFlux()
    }

    fun isUserInConference(userCredentials: UserCredentials): Mono<Boolean> {
        return Mono.just(usersConference.containsKey(userCredentials))
    }

    fun getConference(conferenceId: String): Mono<Conference> {
        return conferences[conferenceId].toMono()
            .switchIfEmpty(Mono.error(ConferenceNotFoundException(conferenceId)))
    }

    fun leaveConference(userCredentials: UserCredentials, conference: Conference): Mono<Conference> {
        return Mono.just(conference).map {
            this.usersConference[userCredentials]?.remove(conference)
            this.conferences[conference.conferenceId]!!.removeUser(userCredentials)
        }
    }

    fun deleteConference(conference: Conference): Mono<Conference> {
        return Mono.justOrEmpty(conference).doOnNext {
            this.conferences.remove(conference.conferenceId)
            this.usersConference.values.forEach {
                it.remove(conference)
            }
        }
    }

    fun removeFromConferences(userCredentials: UserCredentials): Flux<Conference> {
        return this.usersConference[userCredentials]?.toFlux()?.flatMap {
            this.leaveConference(userCredentials, it)
        } ?: Flux.empty()
    }

    fun changeVisibility(conferenceInfo: ConferenceInfo): Mono<Conference> {
        val conference = this.conferences[conferenceInfo.conferenceId]
        conference!!.visible = conferenceInfo.visible
        return conference.toMono()
    }

    fun updateConferences(conferences: List<Conference>): Mono<Void> {
        return conferences.toFlux().doOnNext { conference ->
            this.conferences[conference.conferenceId] = conference
        }.then(recomputeUsersConferences())
    }

    private fun recomputeUsersConferences(): Mono<Void> {
        usersConference.clear()
        conferences.values.forEach { conference ->
            conference.attendees.forEach { user ->
                usersConference.computeIfAbsent(user) { LinkedHashSet() }
                    .also { it.add(conference) }
            }
        }
        return Mono.empty()
    }

    fun getConferenceOfTicket(conferenceId: String?): Mono<Conference> {
        return conferences[conferenceId].toMono()
    }
}
