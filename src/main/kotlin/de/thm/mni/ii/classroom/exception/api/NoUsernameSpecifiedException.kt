package de.thm.mni.ii.classroom.exception.api

class NoUsernameSpecifiedException (
    bbbMessageKey: String = "noUsernameSpecified",
    bbbMessage: String = "You must specify a full name for a user to join a meeting."
): ApiException(bbbMessageKey, null, bbbMessage)
