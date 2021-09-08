package de.thm.mni.ii.classroom.model.classroom

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.empty
import reactor.kotlin.core.publisher.toMono
import java.time.Duration

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

    fun getConferencesOfUser(user: User): Flux<Conference> {
        return Flux.fromIterable(conferenceUsers.filter { it.value.contains(user) }.keys)
    }

    fun getLatestConferenceOfUser(user: User): Mono<Conference> {
        return getConferencesOfUser(user).reduce { conf1, conf2 ->
            if (conf1.creation.isAfter(conf2.creation)) {
                conf1
            } else {
                conf2
            }
        }
    }

    fun leaveConference(user: User, conference: Conference): Mono<Void> {
        this.conferenceUsers[conference]!!.remove(user)
        this.usersConference.remove(user, conference)
        if (conferenceUsers[conference]!!.isEmpty()) {
            scheduleDeletion(conference)
        }
        return this.getLatestConferenceOfUser(user)
            .doOnSuccess {
                if (it != null) {
                    this.usersConference[user] = it
                }
            }.then()
    }

    fun scheduleDeletion(conference: Conference) {
        Mono.delay(Duration.ofSeconds(60)).doOnNext {
            if (this.conferenceUsers[conference]!!.isEmpty()) {
                this.conferenceUsers.remove(conference)
            }
        }.subscribe()
    }

}
