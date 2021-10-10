package de.thm.mni.ii.classroom.event

import de.thm.mni.ii.classroom.model.classroom.ConferenceInfo

data class ConferenceEvent(
    val conferenceInfo: ConferenceInfo,
    val conferenceAction: ConferenceAction
) : ClassroomEvent(ConferenceEvent::class.simpleName!!)

enum class ConferenceAction {
    CREATE,
    CLOSE,
    VISIBILITY,
    USER_CHANGE
}
