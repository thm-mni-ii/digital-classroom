package de.thm.mni.ii.classroom.model.api

import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType

@XmlRootElement(name = "response")
@XmlType(propOrder = ["returncode", "messageKey", "message"])
class EndMeetingBBBResponse(
    success: Boolean,
    messageKey: String = "sentEndMeetingRequest",
    message: String = "A request to end the meeting was sent. Please wait a few seconds, and then use the getMeetingInfo or isMeetingRunning API calls to verify that it was ended"
) : SuperMessageBBB(success, messageKey, message) {
    constructor() : this(false)
}
