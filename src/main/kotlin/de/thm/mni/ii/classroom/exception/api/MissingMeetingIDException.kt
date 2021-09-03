package de.thm.mni.ii.classroom.exception.api

class MissingMeetingIDException(
    override val bbbMessageKey: String = "missingParamMeetingID",
    override val bbbMessage: String = "You must specify a meeting ID for the meeting."
): ApiException(bbbMessage)
