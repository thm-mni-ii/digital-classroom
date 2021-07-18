package de.thm.mni.ii.classroom.security.exception

class UnknownUserException(userId: String,
                           meetingId: String,
                           private val bbbMessageKey: String = "userUnknownToClassroom",
                           private val bbbMessage: String = "User $userId unknown to classroom $meetingId!"
): Exception(bbbMessage)
