package de.thm.mni.ii.classroom.model

import de.thm.mni.ii.classroom.security.exception.InvalidMeetingPasswordException
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.collections.HashSet

data class DigitalClassroom(
    val meetingID: String,
    val attendeePW: String,
    val tutorPW: String,
    val moderatorPW: String,
    val meetingName: String,
    val internalMeetingID: String
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
            attendeePW -> user.userRole = UserRole.STUDENT
            moderatorPW -> user.userRole = UserRole.TEACHER
            tutorPW -> user.userRole = UserRole.TUTOR
            else -> throw InvalidMeetingPasswordException(meetingID)
        }
        users.add(user)
        return user
    }


}
