package de.thm.mni.ii.classroom.model

import de.thm.mni.ii.classroom.security.exception.InvalidMeetingPasswordException
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.collections.HashSet

/**
 * Class representing a digital classroom instance.
 */
data class DigitalClassroom(
    val meetingID: String,
    val studentPassword: String,
    val tutorPassword: String,
    val teacherPassword: String,
    val classroomName: String,
    val internalClassroomId: String
) {

    private val users = HashSet<User>()
    private val tickets = HashSet<Ticket>()

    val creationTimestamp: ZonedDateTime = ZonedDateTime.now()

    fun hasUserJoined() = users.isNotEmpty()
    fun hasBeenForciblyEnded() = false
    fun getDuration() = ChronoUnit.MINUTES.between(creationTimestamp, ZonedDateTime.now())

    fun doesUserExist(user: User): Boolean = users.contains(user)

    fun joinUser(password: String, user: User): User {
        when (password) {
            studentPassword -> user.userRole = UserRole.STUDENT
            teacherPassword -> user.userRole = UserRole.TEACHER
            tutorPassword -> user.userRole = UserRole.TUTOR
            else -> throw InvalidMeetingPasswordException(meetingID)
        }
        users.add(user)
        return user
    }


}
