package de.thm.mni.ii.classroom.security.classroom

import de.thm.mni.ii.classroom.model.User
import de.thm.mni.ii.classroom.properties.JWTProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.jackson.io.JacksonDeserializer
import io.jsonwebtoken.jackson.io.JacksonSerializer
import io.jsonwebtoken.lang.Maps
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*


@Component
class ClassroomJWTService(private val jwtProperties: JWTProperties) {

    private val key: Key = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())

    fun authorize(jwt: String): User? {
        val claims: Claims =
            Jwts.parserBuilder()
                .setSigningKey(key)
                .requireSubject(jwtProperties.jwtSubject)
                .build()
                .parseClaimsJws(jwt)
                .body
        return User(claims)
    }

    fun createToken(user: User): String {
        return Jwts.builder()
            .setSubject(jwtProperties.jwtSubject)
            .addClaims(user.getJwtClaims())
            .setIssuedAt(Date())
            .setExpiration(Date(Date().time + (1000 * jwtProperties.expiration)))
            .signWith(key)
            .compact()
    }

}