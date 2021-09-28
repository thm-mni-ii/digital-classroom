package de.thm.mni.ii.classroom.model.api

import de.thm.mni.ii.classroom.config.bbbFormatter
import de.thm.mni.ii.classroom.model.classroom.DigitalClassroom
import java.time.ZonedDateTime
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType

@XmlRootElement(name = "response")
@XmlType(
    propOrder = [
        "returncode", "meetingID", "internalMeetingID", "parentMeetingID", "attendeePW", "tutorPW", "moderatorPW",
        "createTime", "voiceBridge", "dialNumber", "createDate", "hasUserJoined", "duration", "hasBeenForciblyEnded", "messageKey", "message"
    ]
)
class CreateRoomBBB(
    @field:XmlElement private val meetingID: String,
    @field:XmlElement private val internalMeetingID: String,
    @field:XmlElement private val attendeePW: String,
    @field:XmlElement private val tutorPW: String,
    @field:XmlElement private val moderatorPW: String,
    creationTimestamp: ZonedDateTime,
    success: Boolean = true,
    @field:XmlElement private val parentMeetingID: String = "bbb-none",
    @field:XmlElement private val voiceBridge: String = "",
    @field:XmlElement private val dialNumber: String = "",
    @field:XmlElement private val hasUserJoined: Boolean = false,
    @field:XmlElement private val duration: Long = 0,
    @field:XmlElement private val hasBeenForciblyEnded: Boolean = false,
    messageKey: String = "",
    message: String = ""
) : SuperMessageBBB(success, messageKey, message) {
    @XmlElement private val createTime = creationTimestamp.toInstant().toEpochMilli()
    @XmlElement private val createDate = creationTimestamp.format(bbbFormatter)

    /**
     * Dummy constructor for JAXB Serialization
     */
    constructor() : this("", "", "", "", "", ZonedDateTime.now(), false)

    constructor(instance: DigitalClassroom, messageKey: String = "", message: String = "") : this(
        instance.classroomId,
        instance.classroomId,
        instance.studentPassword,
        instance.tutorPassword,
        instance.teacherPassword,
        instance.creationTimestamp,
        hasUserJoined = instance.hasUserJoined(),
        duration = instance.getDuration(),
        hasBeenForciblyEnded = instance.hasBeenForciblyEnded(),
        messageKey = messageKey,
        message = message
    )
}
