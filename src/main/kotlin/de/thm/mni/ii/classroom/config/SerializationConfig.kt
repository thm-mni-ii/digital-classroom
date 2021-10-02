package de.thm.mni.ii.classroom.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.xml.Jaxb2XmlDecoder
import org.springframework.http.codec.xml.Jaxb2XmlEncoder
import org.springframework.web.reactive.config.WebFluxConfigurer
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * DateTimeFormatter for BBB API-like responses.
 */
val bbbFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US)

/**
 * Configuration for XML & JSON related serialization with Jackson (JSON) or JAX-B (XML).
 */
@Configuration
class SerializationConfig : WebFluxConfigurer {

    /**
     * Jackson object mapper for JSON serialization.
     */
    @Bean
    fun objectMapper(): ObjectMapper {
        val om = ObjectMapper()
        om.registerModule(JavaTimeModule())
        om.registerModule(KotlinModule())
        return om
    }

    /**
     * HTTP message codec configuration. Adds JAX-B XML decoder and encoder.
     */
    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
        configurer.registerDefaults(true)
        configurer.customCodecs().register(Jaxb2XmlDecoder())
        configurer.customCodecs().register(Jaxb2XmlEncoder())
    }
}

/**
 * Jackson Json Serializer for ZonedDateTime as milliseconds since epoch.
 * @see JsonSerializer
 */
class ZonedDateTimeMillisSerializer : JsonSerializer<ZonedDateTime>() {
    override fun serialize(dateTime: ZonedDateTime, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeNumber(dateTime.toInstant().toEpochMilli())
    }
}

/**
 * Jackson Json Deserializer for ZonedDateTime from milliseconds since epoch.
 * @see JsonDeserializer
 */
class ZonedDateTimeMillisDeserializer : JsonDeserializer<ZonedDateTime>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext?): ZonedDateTime {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(parser.longValue), ZoneId.systemDefault())
    }
}
