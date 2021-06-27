package de.thm.mni.ii.classroom.exception

class InvalidMeetingPasswordException(
    meetingID: String,
    private val bbbMessageKey: String = "invalidMeetingPassword",
    private val bbbMessage: String = "Given password does not match any valid for meeting $meetingID!"
): Exception(bbbMessage)
