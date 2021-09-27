package de.thm.mni.ii.classroom.security.jwt

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.MACSigner
import de.thm.mni.ii.classroom.model.classroom.User
import de.thm.mni.ii.classroom.properties.JwtProperties
import de.thm.mni.ii.classroom.util.repeatLength
import net.minidev.json.JSONObject
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import javax.crypto.spec.SecretKeySpec

@Component
class ClassroomJwtService(jwtProperties: JwtProperties) : ReactiveJwtDecoder {

    private val secretKey = SecretKeySpec(jwtProperties.secret.repeatLength(60).toByteArray(), "HmacSHA384")
    private val reactiveJwtDecoder = NimbusReactiveJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS384).build()
    private val jwsSigner = MACSigner(secretKey)

    fun createToken(user: User): Mono<String> {
        return Mono.defer {
            val jwsObject = JWSObject(JWSHeader(JWSAlgorithm.HS384), Payload(JSONObject(user.getJwtClaims())))
            jwsObject.sign(jwsSigner)
            Mono.just(jwsObject.serialize())
        }
    }

    override fun decode(token: String): Mono<Jwt> {
        return reactiveJwtDecoder.decode(token)
    }

    fun decodeToUser(token: String): Mono<User> {
        return this.decode(token).map {
            User(it.claims)
        }
    }
}
