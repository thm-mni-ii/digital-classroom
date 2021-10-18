package de.thm.mni.ii.classroom.exception.api

class MissingMeetingIDException(
    bbbMessageKey: String = "missingParamMeetingID",
    bbbMessage: String = "You must specify a meeting ID for the meeting."
) : ApiException(bbbMessageKey, null, bbbMessage)
