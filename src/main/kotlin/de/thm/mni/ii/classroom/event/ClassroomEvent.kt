package de.thm.mni.ii.classroom.event

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.io.Serializable

@Suppress("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "eventName")
@JsonSubTypes(
    JsonSubTypes.Type(value = UserEvent::class, name = "UserEvent"),
    JsonSubTypes.Type(value = TicketEvent::class, name = "TicketEvent"),
    JsonSubTypes.Type(value = ConferenceEvent::class, name = "ConferenceEvent"),
    JsonSubTypes.Type(value = InvitationEvent::class, name = "InvitationEvent"),
)
abstract class ClassroomEvent(
    private val eventName: String
    ) : Serializable {
    @JsonIgnore abstract fun getClassroomId(): String
    }
