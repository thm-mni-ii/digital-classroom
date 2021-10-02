package de.thm.mni.ii.classroom.model.api

import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlTransient
import javax.xml.bind.annotation.XmlType

@Suppress("unused")
@XmlTransient
@XmlType(propOrder = ["returncode"])
abstract class ReturnCodeBBB(
    success: Boolean
) {
    @XmlElement val returncode: String = if (success) "SUCCESS" else "FAILED"

    /**
     * Dummy constructor for JAXB Serialization
     */
    constructor() : this(false)
}
