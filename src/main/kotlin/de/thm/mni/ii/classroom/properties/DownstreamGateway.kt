package de.thm.mni.ii.classroom.properties

import org.hibernate.validator.constraints.Length
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import javax.validation.constraints.NotBlank

/**
 * upstream.bbb.service-url = ${BBB_URI:https://fk-vv.mni.thm.de/bigbluebutton/api}
 * upstream.bbb.shared-secret = ${BBB_SECRET:8Dsupersecurekeydf0}
 * upstream.bbb.origin-name = ${BBB_ORIGIN_NAME:localhost}
 * upstream.bbb.origin-version = ${BBB_ORIGIN_VERSION:v2}
 */

@ConfigurationProperties(prefix = "downstream.gateway")
@ConstructorBinding
data class DownstreamGateway(

    @NotBlank val sharedSecret: String,

)
