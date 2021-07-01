package de.thm.mni.ii.classroom.downstream.model

import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlTransient
import javax.xml.bind.annotation.XmlType

@XmlTransient
@XmlType(propOrder=["returncode"])
abstract class ReturnCodeBBB(
    success: Boolean
) {
    @XmlElement protected val returncode: String = if (success) "SUCCESS" else "FAILED"

    /**
     * Dummy constructor for JAXB Serialization
     */
    constructor(): this(false)
}
