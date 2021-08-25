package de.thm.mni.ii.classroom.security.jwt

import de.thm.mni.ii.classroom.model.classroom.User
import org.springframework.core.convert.converter.Converter
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class JwtClassroomAuthenticationConverter: Converter<Jwt, ClassroomAuthentication> {

    override fun convert(jwt: Jwt): ClassroomAuthentication {
        val user = User(jwt.claims)
        return ClassroomAuthentication(user, jwt.tokenValue)
    }

}

@Component
class JwtClassroomAuthenticationConverterAdapter(private val delegate: JwtClassroomAuthenticationConverter): Converter<Jwt, Mono<ClassroomAuthentication>> {

    override fun convert(jwt: Jwt): Mono<ClassroomAuthentication> {
        return Mono.just(jwt).map(this.delegate::convert)
    }

}
