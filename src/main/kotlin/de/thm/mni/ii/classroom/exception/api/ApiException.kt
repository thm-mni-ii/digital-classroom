package de.thm.mni.ii.classroom.exception.api

open class ApiException(
    val bbbMessageKey: String = "InternalServerError",
    cause: Throwable? = null,
    val bbbMessage: String = "An error occurred: ${cause?.message}",
    ): Exception(bbbMessage, cause)
