package de.thm.mni.ii.classroom.model.api

import de.thm.mni.ii.classroom.model.classroom.UserCredentials
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType

@XmlRootElement(name = "attendees")
class Attendees(userCredentials: Set<UserCredentials>? = null) {

    @Suppress("unused")
    @XmlElement(name = "attendee")
    private val attendees = userCredentials?.map(::Attendee)
}

@XmlType(
    name = "attendee",
    propOrder = [
        "userID",
        "fullName",
        "role",
        "isPresenter",
        "isListeningOnly",
        "hasJoinedVoice",
        "hasVideo",
        "clientType"
    ]
)

@Suppress("unused")
class Attendee(userCredentials: UserCredentials? = null) {
    @XmlElement private val userID: String? = userCredentials?.userId
    @XmlElement private val fullName: String? = userCredentials?.fullName
    @XmlElement private val role: String? = userCredentials?.userRole?.name
    @XmlElement private val isPresenter = false
    @XmlElement private val isListeningOnly = true
    @XmlElement private val hasJoinedVoice = false
    @XmlElement private val hasVideo = false
    @XmlElement private val clientType = "CLASSROOM"
}
