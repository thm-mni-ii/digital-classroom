package de.thm.mni.ii.classroom.downstream.model

import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlTransient
import javax.xml.bind.annotation.XmlType

@XmlTransient
open class SuperMessageBBB(
    success: Boolean,
    @field:XmlElement protected val messageKey: String,
    @field:XmlElement protected val message: String
): ReturnCodeBBB(success)

@XmlRootElement(name = "response")
@XmlType(propOrder=["returncode", "messageKey", "message"])
class MessageBBB(
    success: Boolean,
    messageKey: String,
    message: String
): SuperMessageBBB(success, messageKey, message) {
    constructor(): this(false, "", "")
}
