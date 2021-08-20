package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.model.api.MessageBBB
import de.thm.mni.ii.classroom.model.classroom.Conference
import de.thm.mni.ii.classroom.model.classroom.DigitalClassroom
import de.thm.mni.ii.classroom.model.classroom.User
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

    fun createConference(digitalClassroom: DigitalClassroom, user: User): Mono<Conference> {
        val conference = Conference(digitalClassroom, user)
        val queryParams = mapOf(
            Pair("meetingID", conference.conferenceId),
            Pair("name", conference.conferenceName),
            Pair("attendeePW", conference.attendeePassword),
            Pair("moderatorPW", conference.moderatorPassword)
        )
        val request = buildApiRequest("create", queryParams)
        return Mono.create { sink ->
            WebClient.create(request).get().retrieve().toEntity(MessageBBB::class.java).subscribe {
                if (it.body?.returncode == "SUCCESS") sink.success(conference)
                else sink.error(Exception(it.body?.message))
            }
        }
    }

    fun joinConference(conference: Conference, user: User, asModerator: Boolean): Mono<String> {
        val queryParams = mapOf(
            Pair("meetingID", conference.conferenceId),
            Pair("fullName", user.fullName),
            Pair("userID", user.userId),
            Pair("password", if (asModerator) conference.moderatorPassword else conference.attendeePassword)
        )
        return Mono.just(buildApiRequest("join", queryParams))
        /*return Mono.create { sink ->
            WebClient.create(request).get().retrieve().toEntity(JoinRoomBBBResponse::class.java)
                .subscribe {
                    if (it.body?.returncode == "SUCCESS") {
                        sink.success(it.body!!.url)
                    } else sink.error(Exception(it.body!!.message))
                }
        }*/
    }

    private fun buildApiRequest(method: String, queryParams: Map<String, String>): String {
        val uriBuilder = UriComponentsBuilder.newInstance()
        queryParams.forEach { (name, value) ->
            uriBuilder.queryParam(name, value.replace(" ", "+"))
        }
        val query = uriBuilder.encode().build().query!!
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