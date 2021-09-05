package de.thm.mni.ii.classroom.model.api

import de.thm.mni.ii.classroom.model.classroom.User
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType

@XmlRootElement(name = "attendee")
@XmlType(propOrder=[
    "userID",
    "fullName",
    "role",
    "isPresenter",
    "isListeningOnly",
    "hasJoinedVoice",
    "hasVideo",
    "clientType"
])
class Attendee(user: User?) {
    @XmlElement private val userID: String? = user?.userId
    @XmlElement private val fullName: String? = user?.fullName
    @XmlElement private val role: String? = user?.userRole?.name
    @XmlElement private val isPresenter = false
    @XmlElement private val isListeningOnly = true
    @XmlElement private val hasJoinedVoice = false
    @XmlElement private val hasVideo = false
    @XmlElement private val clientType = "CLASSROOM"

    constructor(): this(null)

}
