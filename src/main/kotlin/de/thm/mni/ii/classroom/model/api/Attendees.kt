package de.thm.mni.ii.classroom.model.api

import de.thm.mni.ii.classroom.model.classroom.User
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType

@XmlRootElement(name = "attendees")
class Attendees(users: Set<User>? = null) {

    @Suppress("unused")
    @XmlElement(name = "attendee")
    val attendees = users?.mapTo(HashSet(), ::Attendee)
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
class Attendee(user: User? = null) {
    @XmlElement val userID: String? = user?.userId
    @XmlElement val fullName: String? = user?.fullName
    @XmlElement val role: String? = user?.userRole?.name
    @XmlElement val isPresenter = false
    @XmlElement val isListeningOnly = true
    @XmlElement val hasJoinedVoice = false
    @XmlElement val hasVideo = false
    @XmlElement val clientType = "CLASSROOM"
}
