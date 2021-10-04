package de.thm.mni.ii.classroom.model.api

import de.thm.mni.ii.classroom.config.bbbFormatter
import de.thm.mni.ii.classroom.model.classroom.DigitalClassroom
import java.time.ZonedDateTime
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType

@Suppress("unused")
@XmlRootElement(name = "response")
@XmlType(
    propOrder = [
        "returncode",
        "meetings",
        "messageKey",
        "message"
    ]
)
class GetMeetingsBBBResponse(
    digitalClassrooms: List<DigitalClassroom>? = listOf(),
    success: Boolean = true
) : SuperMessageBBB(
    success,
    messageKey = if (digitalClassrooms.isNullOrEmpty()) "noMeetings" else null,
    message = if (digitalClassrooms.isNullOrEmpty()) "no meetings were found on this server" else null
) {
    @XmlElement(required = true, nillable = true) private val meetings = Meetings(digitalClassrooms)
}

@Suppress("unused")
@XmlRootElement(name = "meetings")
class Meetings(digitalClassrooms: List<DigitalClassroom>? = null) {

    @XmlElement(name = "meeting")
    private val meetings = digitalClassrooms?.map(::Meeting)
}

@Suppress("unused")
@XmlType(
    name = "meeting",
    propOrder = [
        "meetingName",
        "meetingID",
        "internalMeetingID",
        "createTime",
        "createDate",
        "voiceBridge",
        "dialNumber",
        "attendeePW",
        "tutorPW",
        "moderatorPW",
        "running",
        "duration",
        "hasUserJoined",
        "recording",
        "hasBeenForciblyEnded",
        "startTime",
        "endTime",
        "participantCount",
        "listenerCount",
        "voiceParticipantCount",
        "videoCount",
        "maxUsers",
        "moderatorCount",
        "attendees",
        "metadata",
        "isBreakout"
    ]
)
class Meeting(digitalClassroom: DigitalClassroom? = null) {
    @XmlElement private val meetingName: String? = digitalClassroom?.classroomName
    @XmlElement private val meetingID: String? = digitalClassroom?.classroomId
    @XmlElement private val internalMeetingID: String? = digitalClassroom?.classroomId
    @XmlElement private val createTime: Long? = digitalClassroom?.creationTimestamp?.toInstant()?.toEpochMilli()
    @XmlElement private val createDate: String? = digitalClassroom?.creationTimestamp!!.format(bbbFormatter)
    @XmlElement private val voiceBridge: String = ""
    @XmlElement private val dialNumber: String = ""
    @XmlElement private val attendeePW: String? = digitalClassroom?.studentPassword
    @XmlElement private val tutorPW: String? = digitalClassroom?.tutorPassword
    @XmlElement private val moderatorPW: String? = digitalClassroom?.teacherPassword
    @XmlElement private val running: Boolean = true
    @XmlElement private val duration: Long = ZonedDateTime.now().toInstant().toEpochMilli() - createTime!!
    @XmlElement private val hasUserJoined: Boolean = false
    @XmlElement private val recording: Boolean = false
    @XmlElement private val hasBeenForciblyEnded: Boolean = false
    @XmlElement private val startTime: Long? = digitalClassroom?.creationTimestamp?.toInstant()?.toEpochMilli()
    @XmlElement private val endTime: Long = 0L
    @XmlElement private val participantCount = digitalClassroom?.getUsers()?.size
    @XmlElement private val listenerCount = digitalClassroom?.getUsers()?.size
    @XmlElement private val voiceParticipantCount = 0
    @XmlElement private val videoCount = digitalClassroom?.getUsers()?.size
    @XmlElement private val maxUsers = 100
    @XmlElement private val moderatorCount = digitalClassroom?.getUsers()?.filter { it.isPrivileged() }?.size
    @XmlElement private val attendees = Attendees(digitalClassroom?.getUsers())
    @XmlElement private val metadata = listOf<String>()
    @XmlElement private val isBreakout = false
}
