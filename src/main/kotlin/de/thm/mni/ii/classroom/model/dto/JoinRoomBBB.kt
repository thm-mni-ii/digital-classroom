package de.thm.mni.ii.classroom.model.dto

import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType

@XmlRootElement(name = "response")
@XmlType(propOrder=["returncode", "messageKey", "message", "meetingID", "userID", "authToken", "sessionToken", "url"])
class JoinRoomBBB(success: Boolean = true,
                  messageKey: String = "",
                  message: String = "",
                  @field:XmlElement private val meetingID: String = "",
                  @field:XmlElement private val userID: String = "",
                  @field:XmlElement private val authToken: String = "",
                  @field:XmlElement private val sessionToken: String = "",
                  @field:XmlElement private val url: String = "",
): SuperMessageBBB(success, messageKey, message)
