package de.thm.mni.ii.classroom.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import javax.validation.constraints.NotBlank

@ConfigurationProperties(prefix = "classroom")
data class ClassroomProperties(

    /**
     * Service host with protocol and hostname / ip address
     */
    @NotBlank val host: String,

    /**
     * Prefix path appended to the host. May be resolved via reverse proxy.
     */
    val prefixPath: String = "",

    @NotBlank val sharedSecret: String,

)
