package de.thm.mni.ii.classroom.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.xml.Jaxb2XmlDecoder
import org.springframework.http.codec.xml.Jaxb2XmlEncoder
import org.springframework.web.reactive.config.WebFluxConfigurer
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

val bbbFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US)

@Configuration
class SerializationConfig: WebFluxConfigurer {

    @Bean
    fun objectMapper(): ObjectMapper {
        val mapper = ObjectMapper().registerKotlinModule()
        return mapper
    }

    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
        configurer.registerDefaults(true)
        configurer.customCodecs().register(Jaxb2XmlDecoder())
        configurer.customCodecs().register(Jaxb2XmlEncoder())
    }

}

class ZonedDateTimeMillisSerializer: JsonSerializer<ZonedDateTime>() {
    override fun serialize(dateTime: ZonedDateTime, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeNumber(dateTime.toInstant().toEpochMilli())
    }
}

class ZonedDateTimeMillisDeserializer: JsonDeserializer<ZonedDateTime>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext?): ZonedDateTime {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(parser.longValue), ZoneId.systemDefault())
    }
}