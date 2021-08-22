package de.thm.mni.ii.classroom.security.rsocket

import de.thm.mni.ii.classroom.properties.JWTProperties
import de.thm.mni.ii.classroom.util.repeatLength
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import javax.crypto.spec.SecretKeySpec

@Component
class CustomJwtDecoder(private val props: JWTProperties): ReactiveJwtDecoder {

    private val reactiveJwtDecoder = getAccessTokenDecoder()

    private fun getAccessTokenDecoder(): ReactiveJwtDecoder? {
        val secretKey = SecretKeySpec(
            props.secret.repeatLength(60).toByteArray(),
            "HmacSHA384"
        )
        return NimbusReactiveJwtDecoder.withSecretKey(secretKey)
            .macAlgorithm(MacAlgorithm.HS384)
            .build()
    }

    @Throws(JwtException::class)
    override fun decode(token: String): Mono<Jwt> {
        return reactiveJwtDecoder!!.decode(token)
    }
}
