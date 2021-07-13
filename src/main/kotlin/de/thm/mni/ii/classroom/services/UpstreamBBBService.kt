package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.downstream.model.JoinRoomBBBResponse
import de.thm.mni.ii.classroom.model.Conference
import de.thm.mni.ii.classroom.model.DigitalClassroom
import de.thm.mni.ii.classroom.model.User
import de.thm.mni.ii.classroom.properties.UpstreamBBBProperties
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono

@Component
class UpstreamBBBService(private val upstreamBBBProperties: UpstreamBBBProperties) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private val webClient = WebClient.create(upstreamBBBProperties.serviceUrl)

    fun createConference(digitalClassroom: DigitalClassroom): Mono<Conference> {
        val conference = Conference(digitalClassroom)
        val queryParams = mapOf(
            Pair("meetingId", conference.conferenceId),
            Pair("name", conference.conferenceName),
            Pair("attendeePW", conference.attendeePassword),
            Pair("moderatorPW", conference.moderatorPassword)
        )
        val request = buildApiRequest("create", queryParams)
        return Mono.create { sink ->
            WebClient.create(request).get().exchangeToMono { exchange ->
                if (exchange.statusCode().isError) exchange.createException().map(sink::error)
                else sink.success(conference)
                Mono.empty<Unit>()
            }
        }
    }

    fun joinConference(conference: Conference, user: User, asModerator: Boolean): Mono<String> {
        val queryParams = mapOf(
            Pair("meetingId", conference.conferenceId),
            Pair("fullName", user.fullName),
            Pair("userID", user.userId),
            Pair("password", if (asModerator) conference.moderatorPassword else conference.attendeePassword)
        )
        val request = buildApiRequest("join", queryParams)
        return WebClient.create(request).get().exchangeToMono { exchange ->
            if (exchange.statusCode().isError) exchange.createException().flatMap {
                Mono.error(it)
            } else exchange.bodyToMono(JoinRoomBBBResponse::class.java).map { it.url }
        }
    }

    private fun buildApiRequest(method: String, queryParams: Map<String, String>): String {
        val uriBuilder = UriComponentsBuilder.newInstance()
        queryParams.forEach { (name, value) ->
            uriBuilder.queryParam(name, value)
        }
        val query = uriBuilder.build().query!!.substring(1)
        val checksum = calculateChecksum(method, query, upstreamBBBProperties.sharedSecret)
        uriBuilder.queryParam("checksum", checksum)
        val queryWithChecksum = uriBuilder.build().query!!
        return "${upstreamBBBProperties.serviceUrl}/api/$method$queryWithChecksum"
    }

    private fun calculateChecksum(method: String, query: String, secret: String): String {
        return DigestUtils.sha1Hex("$method$query$secret")
    }

}