package de.thm.mni.ii.classroom.model

import org.apache.commons.lang3.RandomStringUtils
import org.springframework.util.MultiValueMap
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

data class DigitalClassroomInstance(
    val meetingID: String,
    val attendeePW: String,
    val moderatorPW: String,
    val meetingName: String,
    val internalMeetingID: String = "${RandomStringUtils.randomAlphanumeric(40)}-${RandomStringUtils.randomAlphanumeric(13)}",
) {

    private val users = HashSet<User>()
    private val tickets = HashSet<Ticket>()
    val creationTimestamp: ZonedDateTime = ZonedDateTime.now()

    fun hasUserJoined() = users.isNotEmpty()
    fun hasBeenForciblyEnded() = false
    fun getDuration() = ChronoUnit.MINUTES.between(creationTimestamp, ZonedDateTime.now())

    constructor(param: MultiValueMap<String, String>): this(
        meetingID = param.getFirst("meetingID")!!,
        attendeePW = param.getFirst("attendeePW") ?: RandomStringUtils.randomAlphanumeric(30),
        moderatorPW = param.getFirst("moderatorPW") ?: RandomStringUtils.randomAlphanumeric(30),
        meetingName = param.getFirst("name") ?: "Digital Classroom"
    )

}
