package de.thm.mni.ii.classroom.model.dto

import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType

@XmlRootElement(name = "response")
@XmlType(propOrder=["returncode", "running"])
class IsMeetingRunningBBB(
    @field:XmlElement private val running: Boolean,
    success: Boolean = true
): ReturnCodeBBB(success) {
    /**
     * Dummy constructor for JAXB Serialization
     */
    constructor(): this(false, false)
}
