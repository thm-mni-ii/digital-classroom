package de.thm.mni.ii.classroom.mapper

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider

class BBBReturnCodeSerializer: JsonSerializer<Boolean>() {

    override fun serialize(value: Boolean, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(if (value) "SUCCESS" else "FAILED")
    }

}

class BBBReturnCodeDeserializer: JsonDeserializer<Boolean>() {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Boolean {
        return p.text == "SUCCESS"
    }

}
