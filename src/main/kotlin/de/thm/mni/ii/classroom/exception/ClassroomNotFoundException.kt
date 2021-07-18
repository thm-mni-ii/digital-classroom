package de.thm.mni.ii.classroom.exception

class ClassroomNotFoundException(
        meetingID: String = "",
        private val bbbMessageKey: String = "meetingDoesNotExist",
        private val bbbMessage: String = "MeetingID $meetingID does not exist!"
    ): Exception(bbbMessage)
