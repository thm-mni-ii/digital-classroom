package de.thm.mni.ii.classroom.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import javax.validation.constraints.NotBlank

@ConfigurationProperties(prefix = "upstream.bbb")
data class UpstreamBBBProperties(

    @NotBlank val serviceUrl: String,
    @NotBlank val sharedSecret: String

)
