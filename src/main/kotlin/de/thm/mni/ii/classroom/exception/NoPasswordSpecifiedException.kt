package de.thm.mni.ii.classroom.exception

class NoPasswordSpecifiedException(
    private val bbbMessageKey: String = "noPasswordSpecified",
    private val bbbMessage: String = "No password for joining a meeting was specified!"
): Exception(bbbMessage)
