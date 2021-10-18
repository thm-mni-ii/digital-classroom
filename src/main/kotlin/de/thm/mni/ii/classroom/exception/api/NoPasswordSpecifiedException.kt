package de.thm.mni.ii.classroom.exception.api

class NoPasswordSpecifiedException(
    bbbMessageKey: String = "noPasswordSpecified",
    bbbMessage: String = "No password for joining a meeting was specified!"
) : ApiException(bbbMessageKey, null, bbbMessage)
