package de.thm.mni.ii.classroom.exception

class MissingMeetingIDException(
    private val bbbMessageKey: String = "missingParamMeetingID",
    private val bbbMessage: String = "You must specify a meeting ID for the meeting."
): Exception("No meetingID given!")
