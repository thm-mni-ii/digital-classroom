package de.thm.mni.ii.classroom.util

import de.thm.mni.ii.classroom.exception.MissingMeetingIDException
import org.springframework.util.MultiValueMap

fun getMeetingID(param: MultiValueMap<String, String>): String {
    val meetingID = param.getFirst("meetingID")
    if (meetingID.isNullOrEmpty()) {
        throw MissingMeetingIDException()
    }
    return meetingID
}
