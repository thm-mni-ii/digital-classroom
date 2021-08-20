package de.thm.mni.ii.classroom.security.classroom

import de.thm.mni.ii.classroom.model.classroom.User
import de.thm.mni.ii.classroom.properties.JWTProperties
import de.thm.mni.ii.classroom.util.repeatLength
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*


@Component
class ClassroomJWTService(private val jwtProperties: JWTProperties) {

    private val key: Key = Keys.hmacShaKeyFor(jwtProperties.secret.repeatLength(60).toByteArray())

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
            .setExpiration(Date(System.currentTimeMillis() + (1000 * jwtProperties.expiration)))
            .signWith(key)
            .compact()
    }

}