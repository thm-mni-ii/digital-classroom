package de.thm.mni.ii.classroom.security.jwt

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import de.thm.mni.ii.classroom.model.classroom.UserCredentials
import de.thm.mni.ii.classroom.properties.ClassroomProperties
import de.thm.mni.ii.classroom.properties.JwtProperties
import de.thm.mni.ii.classroom.util.repeatLength
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.Date
import javax.crypto.spec.SecretKeySpec

@Component
class ClassroomJwtService(
    private val jwtProperties: JwtProperties,
    private val classroomProperties: ClassroomProperties
) : ReactiveJwtDecoder {

    private fun JWTClaimsSet.Builder.addClaims(claims: Map<String, Any>): JWTClaimsSet.Builder {
        claims.entries.forEach { (key, value) ->
            this.claim(key, value)
        }
        return this
    }

    private val secretKey = SecretKeySpec(jwtProperties.secret.repeatLength(60).toByteArray(), "HmacSHA384")
    private val reactiveJwtDecoder = NimbusReactiveJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS384).build()
    private val jwsSigner = MACSigner(secretKey)

    fun createToken(userCredentials: UserCredentials): Mono<String> {
        return Mono.defer {
            val claimsSet = JWTClaimsSet.Builder()
                .subject(jwtProperties.jwtSubject)
                .issuer(classroomProperties.host)
                .expirationTime(Date(Date().time + jwtProperties.expiration * 1000))
                .addClaims(userCredentials.getJwtClaims())
                .build()
            val signedJwt = SignedJWT(JWSHeader(JWSAlgorithm.HS384), claimsSet)
            signedJwt.sign(jwsSigner)
            Mono.just(signedJwt.serialize())
        }
    }

    override fun decode(token: String): Mono<Jwt> {
        return reactiveJwtDecoder.decode(token)
    }

    fun decodeToUser(token: String): Mono<UserCredentials> {
        return this.decode(token).map {
            UserCredentials(it.claims)
        }
    }
}
