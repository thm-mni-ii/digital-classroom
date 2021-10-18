package de.thm.mni.ii.classroom.model.api

import de.thm.mni.ii.classroom.model.classroom.UserCredentials
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType

@XmlRootElement(name = "attendees")
class Attendees(userCredentials: Set<UserCredentials>? = null) {

    @Suppress("unused")
    @XmlElement(name = "attendee")
    val attendees = userCredentials?.mapTo(LinkedHashSet(), ::Attendee)
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
    @XmlElement val userID: String? = userCredentials?.userId
    @XmlElement val fullName: String? = userCredentials?.fullName
    @XmlElement val role: String? = userCredentials?.userRole?.name
    @XmlElement val isPresenter = false
    @XmlElement val isListeningOnly = true
    @XmlElement val hasJoinedVoice = false
    @XmlElement val hasVideo = false
    @XmlElement val clientType = "CLASSROOM"
}
