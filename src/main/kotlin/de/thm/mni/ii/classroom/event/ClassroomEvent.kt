package de.thm.mni.ii.classroom.event

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "eventName")
@JsonSubTypes(
    JsonSubTypes.Type(value = MessageEvent::class, name = "MessageEvent"),
    JsonSubTypes.Type(value = UserEvent::class, name = "UserEvent"),
    JsonSubTypes.Type(value = TicketEvent::class, name = "TicketEvent"),
    JsonSubTypes.Type(value = ConferenceEvent::class, name = "ConferenceEvent"),
    JsonSubTypes.Type(value = InvitationEvent::class, name = "InvitationEvent"),
    )
abstract class ClassroomEvent(@field:SuppressWarnings("unused") private val eventName: String): Serializable

data class MessageEvent(val message: String): ClassroomEvent(MessageEvent::class.simpleName!!)
