package de.thm.mni.ii.classroom.exception.api

class UnknownUserException(
    userId: String,
    meetingId: String,
    bbbMessageKey: String = "userUnknownToClassroom",
    bbbMessage: String = "User $userId unknown to classroom $meetingId!"
) : ApiException(bbbMessageKey, null, bbbMessage)
