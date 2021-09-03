package de.thm.mni.ii.classroom.exception.api

open class ApiException(
    open val bbbMessageKey: String = "InternalServerError",
    cause: Throwable? = null,
    open val bbbMessage: String = "An error occurred: ${cause?.message}",
    ): Exception(bbbMessage, cause)
