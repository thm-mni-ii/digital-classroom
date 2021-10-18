package de.thm.mni.ii.classroom.event

import com.fasterxml.jackson.annotation.JsonIgnore
import de.thm.mni.ii.classroom.model.classroom.ConferenceInfo

data class ConferenceEvent(
    val conferenceInfo: ConferenceInfo,
    val conferenceAction: ConferenceAction
) : ClassroomEvent(ConferenceEvent::class.simpleName!!) {
    @JsonIgnore override fun getClassroomId(): String = this.conferenceInfo.classroomId
}

enum class ConferenceAction {
    CREATE,
    CLOSE,
    VISIBILITY,
    USER_CHANGE
}
