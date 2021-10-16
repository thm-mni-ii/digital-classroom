package de.thm.mni.ii.classroom.model.api

import de.thm.mni.ii.classroom.config.bbbFormatter
import de.thm.mni.ii.classroom.model.classroom.DigitalClassroom
import java.time.Instant
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
    @XmlElement(required = true, nillable = true) val meetings = Meetings(digitalClassrooms)
}

@Suppress("unused")
@XmlRootElement(name = "meetings")
class Meetings(digitalClassrooms: List<DigitalClassroom>? = null) {

    @XmlElement(name = "meeting")
    val meetings = digitalClassrooms?.map(::Meeting)
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
    @XmlElement val meetingName: String? = digitalClassroom?.classroomName
    @XmlElement val meetingID: String? = digitalClassroom?.classroomId
    @XmlElement val internalMeetingID: String? = digitalClassroom?.classroomId
    @XmlElement val createTime: Long = digitalClassroom?.creationTimestamp?.toInstant()?.toEpochMilli() ?: Instant.now().toEpochMilli()
    @XmlElement val createDate: String? = digitalClassroom?.creationTimestamp?.format(bbbFormatter)
    @XmlElement val voiceBridge: String = ""
    @XmlElement val dialNumber: String = ""
    @XmlElement val attendeePW: String? = digitalClassroom?.studentPassword
    @XmlElement val tutorPW: String? = digitalClassroom?.tutorPassword
    @XmlElement val moderatorPW: String? = digitalClassroom?.teacherPassword
    @XmlElement val running: Boolean = true
    @XmlElement val duration: Long = ZonedDateTime.now().toInstant().toEpochMilli() - createTime
    @XmlElement val hasUserJoined: Boolean = false
    @XmlElement val recording: Boolean = false
    @XmlElement val hasBeenForciblyEnded: Boolean = false
    @XmlElement val startTime: Long? = digitalClassroom?.creationTimestamp?.toInstant()?.toEpochMilli()
    @XmlElement val endTime: Long = 0L
    @XmlElement val participantCount = digitalClassroom?.getUsers()?.size
    @XmlElement val listenerCount = digitalClassroom?.getUsers()?.size
    @XmlElement val voiceParticipantCount = 0
    @XmlElement val videoCount = digitalClassroom?.getUsers()?.size
    @XmlElement val maxUsers = 100
    @XmlElement val moderatorCount = digitalClassroom?.getUsers()?.filter { it.isPrivileged() }?.size
    @XmlElement val attendees = Attendees(digitalClassroom?.getUsers())
    @XmlElement val metadata = MeetingMetaData()
    @XmlElement val isBreakout = false
}

@Suppress("unused")
@XmlType(
    name = "metadata",
    propOrder = [
        "classroomid",
        "creatorid",
        "ticketid"
    ]
)
class MeetingMetaData {
    @XmlElement val classroomid: String = ""
    @XmlElement val creatorid: String = ""
    @XmlElement val ticketid: Long? = null
}
