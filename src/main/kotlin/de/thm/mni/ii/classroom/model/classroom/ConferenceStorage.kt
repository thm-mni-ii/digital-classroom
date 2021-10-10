package de.thm.mni.ii.classroom.model.classroom

import de.thm.mni.ii.classroom.exception.classroom.ConferenceNotFoundException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

class ConferenceStorage {

    private val usersConference = HashMap<User, HashSet<Conference>>()
    private val conferences = HashMap<String, Conference>()

    fun getConferencesOfUser(user: User) =
        Flux.fromIterable(usersConference[user] ?: LinkedHashSet())

    fun getUsersOfConference(conference: Conference): Flux<User> {
        return conferences[conference.conferenceId]?.attendees?.toFlux()
            ?: throw ConferenceNotFoundException(conference.conferenceId)
    }

    fun joinUser(conference: Conference, user: User): Mono<Conference> {
        return Mono.just(user).map {
            usersConference.computeIfAbsent(user) { HashSet() }
                .also { it.add(conference) }
            conferences.computeIfAbsent(conference.conferenceId) { conference }
                .also { it.attendees.add(user) }
        }
    }

    fun createConference(conference: Conference): Mono<Conference> {
        return conferences.computeIfAbsent(conference.conferenceId) { conference }.toMono()
    }

    fun getConferences(): Flux<Conference> {
        return conferences.values.toFlux()
    }

    fun getUsersInConferences(): Flux<User> {
        return Flux.fromIterable(usersConference.keys)
    }

    fun isUserInConference(user: User): Mono<Boolean> {
        return Mono.just(usersConference.containsKey(user))
    }

    fun getConference(conferenceId: String): Mono<Conference> {
        return conferences[conferenceId].toMono()
            .switchIfEmpty(Mono.error(ConferenceNotFoundException(conferenceId)))
    }

    fun leaveConference(user: User, conference: Conference): Mono<Conference> {
        return Mono.just(conference).map {
            this.usersConference[user]?.remove(conference)
            this.conferences[conference.conferenceId]!!.removeUser(user)
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

    fun removeFromConferences(user: User): Flux<Conference> {
        return this.usersConference[user]?.toFlux()?.flatMap {
            this.leaveConference(user, it)
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
                usersConference.computeIfAbsent(user) { HashSet() }
                    .also { it.add(conference) }
            }
        }
        return Mono.empty()
    }
}
