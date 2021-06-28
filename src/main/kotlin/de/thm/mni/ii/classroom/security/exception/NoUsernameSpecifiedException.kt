package de.thm.mni.ii.classroom.security.exception

class NoUsernameSpecifiedException (
    private val bbbMessageKey: String = "noUsernameSpecified",
    private val bbbMessage: String = "You must specify a full name for a user to join a meeting."
): Exception(bbbMessage)
