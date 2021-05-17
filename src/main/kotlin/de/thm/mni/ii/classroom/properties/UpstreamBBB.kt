package de.thm.mni.ii.classroom.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import javax.validation.constraints.NotBlank

@ConfigurationProperties(prefix = "upstream.bbb")
@ConstructorBinding
data class UpstreamBBB(

    @NotBlank val serviceUrl: String,
    @NotBlank val sharedSecret: String,
    @NotBlank val originName: String,
    @NotBlank val originVersion: String

)
