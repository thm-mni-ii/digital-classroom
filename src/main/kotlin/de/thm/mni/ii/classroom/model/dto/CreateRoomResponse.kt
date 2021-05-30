package de.thm.mni.ii.classroom.model.dto

import de.thm.mni.ii.classroom.config.bbbFormatter
import org.apache.commons.lang3.RandomStringUtils
import java.time.ZonedDateTime
import java.util.*
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType


@XmlRootElement(name = "response")
@XmlType(propOrder=["returncode", "meetingID", "internalMeetingID", "parentMeetingID", "attendeePW", "moderatorPW",
    "createTime", "voiceBridge", "dialNumber", "createDate", "hasUserJoined", "duration", "hasBeenForciblyEnded", "messageKey", "message"])
class CreateRoomResponse(@field:XmlElement private val returncode: Boolean = true,
                         @field:XmlElement private val meetingID: String = "",
                         @field:XmlElement private val internalMeetingID: UUID = UUID.randomUUID(),
                         @field:XmlElement private val parentMeetingID: String = "bbb-none",
                         @field:XmlElement private val attendeePW: String? = RandomStringUtils.randomAlphanumeric(30),
                         @field:XmlElement private val moderatorPW: String? = RandomStringUtils.randomAlphanumeric(30),
                         @field:XmlElement private val voiceBridge: String = "",
                         @field:XmlElement private val dialNumber: String = "",
                         @field:XmlElement private val hasUserJoined: Boolean = false,
                         @field:XmlElement private val duration: Int = 0,
                         @field:XmlElement private val hasBeenForciblyEnded: Boolean = false,
                         @field:XmlElement private val messageKey: String = "",
                         @field:XmlElement private val message: String = ""
) {
    private val timestamp: ZonedDateTime = ZonedDateTime.now()

    @XmlElement private val createTime: Long = timestamp.toInstant().toEpochMilli()
    @XmlElement private val createDate: String? = timestamp.format(bbbFormatter)
}
