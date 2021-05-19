package de.thm.mni.ii.classroom.model.dto

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import de.thm.mni.ii.classroom.mapper.BBBReturnCodeDeserializer
import de.thm.mni.ii.classroom.mapper.BBBReturnCodeSerializer


data class CreateRoomResponse(

    @JsonSerialize(using = BBBReturnCodeSerializer::class)
    @JsonDeserialize(using = BBBReturnCodeDeserializer::class)
    private val returncode: Boolean

)
