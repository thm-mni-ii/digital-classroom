package de.thm.mni.ii.classroom.exception.api

class InvalidMeetingPasswordException(
    meetingID: String,
    bbbMessageKey: String = "invalidMeetingPassword",
    bbbMessage: String = "Given password does not match any valid for meeting $meetingID!"
): ApiException(bbbMessageKey, null, bbbMessage)
