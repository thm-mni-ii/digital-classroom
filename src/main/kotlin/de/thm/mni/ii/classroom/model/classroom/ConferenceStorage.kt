package de.thm.mni.ii.classroom.model.classroom

import de.thm.mni.ii.classroom.exception.classroom.ConferenceNotFoundException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.empty
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

class ConferenceStorage(private val digitalClassroom: DigitalClassroom) {

    private val usersConference = HashMap<User, Conference>()
    private val conferenceUsers = HashMap<Conference, HashSet<User>>()

    fun getConferenceOfUser(user: User) = usersConference[user]

    fun getUsersOfConference(conference: Conference): Flux<User> = conferenceUsers[conference]?.toFlux()
        ?: Flux.error(ConferenceNotFoundException(conference.conferenceId))

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

    private fun getConferencesOfUser(user: User): Set<Conference> {
        return conferenceUsers.filter { it.value.contains(user) }.keys
    }

    private fun getLatestConferenceOfUser(user: User): Conference? {
        return getConferencesOfUser(user).reduceOrNull { conf1, conf2 ->
            if (conf1.creation.isAfter(conf2.creation)) {
                conf1
            } else {
                conf2
            }
        }
    }

    fun leaveConference(user: User, conference: Conference) {
        this.conferenceUsers[conference]!!.remove(user)
        this.usersConference.remove(user, conference)
        val otherConference = this.getLatestConferenceOfUser(user)
        if (otherConference != null) {
            this.usersConference[user] = otherConference
        }
    }

    fun deleteConference(conference: Conference): Mono<Void> {
        this.conferenceUsers.remove(conference)
        this.usersConference.values.remove(conference)
        return empty()
    }

}
