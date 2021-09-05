package de.thm.mni.ii.classroom.exception.api

class ClassroomNotFoundException(
        meetingID: String = "",
        bbbMessageKey: String = "meetingDoesNotExist",
        bbbMessage: String = "MeetingID $meetingID does not exist!"
    ): ApiException(bbbMessageKey, null, bbbMessage)
