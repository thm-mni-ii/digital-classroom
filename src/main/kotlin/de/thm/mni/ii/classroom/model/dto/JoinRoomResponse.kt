package de.thm.mni.ii.classroom.model.dto

import de.thm.mni.ii.classroom.config.bbbFormatter
import org.apache.commons.lang3.RandomStringUtils
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType

@XmlRootElement(name = "response")
@XmlType(propOrder=["returncode", "messageKey", "message", "meetingID", "userID", "authToken", "sessionToken", "url"])
class JoinRoomResponse(@field:XmlElement private val returncode: Boolean = true,
                       @field:XmlElement private val messageKey: String = "",
                       @field:XmlElement private val message: String = "",
                       @field:XmlElement private val meetingID: String = "",
                       @field:XmlElement private val userID: String = "",
                       @field:XmlElement private val authToken: String = "",
                       @field:XmlElement private val sessionToken: String = "",
                       @field:XmlElement private val url: String = "",
)
