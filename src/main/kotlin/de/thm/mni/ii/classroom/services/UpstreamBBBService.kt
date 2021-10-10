package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.exception.classroom.ClassroomException
import de.thm.mni.ii.classroom.model.api.GetMeetingsBBBResponse
import de.thm.mni.ii.classroom.model.api.MessageBBB
import de.thm.mni.ii.classroom.model.classroom.Conference
import de.thm.mni.ii.classroom.model.classroom.ConferenceInfo
import de.thm.mni.ii.classroom.model.classroom.DigitalClassroom
import de.thm.mni.ii.classroom.model.classroom.JoinLink
import de.thm.mni.ii.classroom.model.classroom.User
import de.thm.mni.ii.classroom.properties.UpstreamBBBProperties
import de.thm.mni.ii.classroom.util.component1
import de.thm.mni.ii.classroom.util.component2
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.UUID

@Component
class UpstreamBBBService(private val upstreamBBBProperties: UpstreamBBBProperties) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun createConference(user: User, conferenceInfo: ConferenceInfo): Mono<Conference> {
        return Mono.just(
            Conference(
                conferenceInfo.classroomId,
                UUID.randomUUID().toString(),
                conferenceInfo.conferenceName,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                creator = user,
                visible = conferenceInfo.visible,
                attendees = mutableSetOf()
            )
        ).flatMap { conference ->
            val queryParams = mapOf(
                Pair("meetingID", conference.conferenceId),
                Pair("name", conference.conferenceName),
                Pair("attendeePW", conference.attendeePassword),
                Pair("moderatorPW", conference.moderatorPassword),
                Pair("meta_classroomId", user.classroomId),
                Pair("meta_creatorId", user.userId)
            )
            val request = buildApiRequest("create", queryParams)
            Mono.zip(Mono.just(conference), WebClient.create(request).get().retrieve().toEntity(MessageBBB::class.java))
        }.map { (conference, responseEntity) ->
            if (responseEntity.body!!.returncode == "SUCCESS") conference
            else error(Exception(responseEntity.body?.message))
        }
    }

    fun joinConference(conference: Conference, user: User, asModerator: Boolean): Mono<JoinLink> {
        val queryParams = mapOf(
            Pair("meetingID", conference.conferenceId),
            Pair("fullName", user.fullName),
            Pair("userID", user.userId),
            Pair("password", if (asModerator) conference.moderatorPassword else conference.attendeePassword)
        )
        return Mono.just(buildApiRequest("join", queryParams)).map { JoinLink(ConferenceInfo(conference), it) }
    }

    fun endConference(conference: Conference): Mono<MessageBBB> {
        val queryParams = mapOf(
            Pair("meetingID", conference.conferenceId),
            Pair("password", conference.moderatorPassword)
        )
        val request = buildApiRequest("end", queryParams)
        return WebClient.create(request).get().retrieve().toEntity(MessageBBB::class.java)
            .map { it.body!! }
            .map {
                if (it.returncode == "SUCCESS") {
                    it
                } else {
                    error(ClassroomException("Error from upstream BBB: ${it.message}"))
                }
            }
    }

    fun syncMeetings(
        classroom: DigitalClassroom,
        conferences: List<Conference>
    ): Flux<Conference> {
        val request = buildApiRequest("getMeetings", mapOf())
        return WebClient.create(request).get().retrieve()
            .bodyToMono(GetMeetingsBBBResponse::class.java)
            .flatMapMany { getMeetings ->
                Flux.fromIterable(getMeetings.meetings.meetings ?: listOf())
            }.flatMap { meeting ->
                val conference = conferences.find { conference ->
                    conference.conferenceId == meeting.meetingID
                }
                if (conference == null) {
                    Mono.empty()
                } else {
                    Pair(conference, meeting).toMono()
                }
            }.map { (conference, meeting) ->
                val attendingUsers = meeting.attendees.attendees
                ?.mapTo(mutableSetOf()) {
                    classroom.getUser(it.userID!!)
                } ?: mutableSetOf()
                Conference(
                    conference.classroomId,
                    meeting!!.meetingID!!,
                    meeting.meetingName!!,
                    meeting.attendeePW!!,
                    meeting.moderatorPW!!,
                    conference.creator,
                    conference.visible,
                    attendingUsers,
                    conference.creationTimestamp
                )
            }
    }

    private fun buildApiRequest(method: String, queryParams: Map<String, String>): String {
        val uriBuilder = UriComponentsBuilder.newInstance()
        queryParams.forEach { (name, value) ->
            uriBuilder.queryParam(name, value.replace(" ", "+"))
        }
        val query = uriBuilder.encode().build().query ?: ""
        val checksum = calculateChecksum(method, query, upstreamBBBProperties.sharedSecret)
        uriBuilder.queryParam("checksum", checksum)
        val queryWithChecksum = uriBuilder.encode().build().query!!
        return "${upstreamBBBProperties.serviceUrl}/api/$method?$queryWithChecksum"
    }

    private fun calculateChecksum(method: String, query: String, secret: String): String {
        logger.debug("String: $method$query$secret")
        logger.debug("Checksum: ${DigestUtils.sha1Hex("$method$query$secret")}")
        return DigestUtils.sha1Hex("$method$query$secret")
    }
}
