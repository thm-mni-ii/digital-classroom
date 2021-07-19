package de.thm.mni.ii.classroom.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import javax.validation.constraints.NotBlank

@ConfigurationProperties(prefix = "classroom")
@ConstructorBinding
data class ClassroomProperties(

    /**
     * Service url with protocol, hostname and optional prefix path.
     */
    @NotBlank val url: String,

    @NotBlank val sharedSecret: String,

)
