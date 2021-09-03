package de.thm.mni.ii.classroom.properties

import org.hibernate.validator.constraints.Length
import org.hibernate.validator.constraints.Range
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import javax.validation.constraints.NotBlank

@ConfigurationProperties(prefix = "jwt")
@ConstructorBinding
data class JwtProperties(

    /**
     * Secret for JWT signature
     */
    @NotBlank @Length(min = 64) val secret: String,

    /**
     * validity duration of JWT in seconds
     */
    @NotBlank @Range(min = 60) val expiration: Int,

    val jwtSubject: String = "classroom-auth"

)
