package de.thm.mni.ii.classroom.model.api

import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType

@XmlRootElement(name = "response")
@XmlType(propOrder=["returncode", "messageKey", "message", "meetingID", "userID", "authToken", "sessionToken", "url"])
class JoinRoomBBBResponse(success: Boolean = true,
                          messageKey: String = "successfullyJoined",
                          message: String = "You have joined successfully.",
                          @field:XmlElement val meetingID: String = "",
                          @field:XmlElement val userID: String = "",
                          @field:XmlElement val authToken: String = "",
                          @field:XmlElement val sessionToken: String = "",
                          @field:XmlElement val url: String = "",
): SuperMessageBBB(success, messageKey, message)
