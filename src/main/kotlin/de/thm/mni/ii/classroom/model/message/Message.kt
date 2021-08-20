package de.thm.mni.ii.classroom.model.message

import java.time.LocalDateTime

data class Message(
    val jwt: String,
    val timestamp: LocalDateTime,
    val message: String
)
