package de.thm.mni.ii.classroom.model.api

import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlTransient
import javax.xml.bind.annotation.XmlType

@Suppress("unused")
@XmlTransient
open class SuperMessageBBB(
    success: Boolean,
    @field:XmlElement val messageKey: String?,
    @field:XmlElement val message: String?
) : ReturnCodeBBB(success)

@Suppress("unused")
@XmlRootElement(name = "response")
@XmlType(propOrder = ["returncode", "messageKey", "message"])
class MessageBBB(
    success: Boolean,
    messageKey: String?,
    message: String?
) : SuperMessageBBB(success, messageKey, message) {
    constructor() : this(false, null, null)
}
