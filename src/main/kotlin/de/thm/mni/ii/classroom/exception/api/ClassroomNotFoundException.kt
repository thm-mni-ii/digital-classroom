package de.thm.mni.ii.classroom.exception.api

class ClassroomNotFoundException(
        meetingID: String = "",
        override val bbbMessageKey: String = "meetingDoesNotExist",
        override val bbbMessage: String = "MeetingID $meetingID does not exist!"
    ): ApiException(bbbMessage)
