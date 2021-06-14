package de.thm.mni.ii.classroom.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import javax.validation.constraints.NotBlank

@ConfigurationProperties(prefix = "downstream.gateway")
@ConstructorBinding
data class DownstreamGatewayProperties(

    @NotBlank val sharedSecret: String,

)
