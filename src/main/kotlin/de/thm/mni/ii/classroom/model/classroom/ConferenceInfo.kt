package de.thm.mni.ii.classroom.model.classroom

import de.thm.mni.ii.classroom.model.ClassroomDependent

open class ConferenceInfo(
    override val classroomId: String,
    open val conferenceId: String?,
    open val conferenceName: String,
    open val creator: User?,
    open val visible: Boolean,
): ClassroomDependent {
    constructor(conference: Conference): this(
        conference.classroomId,
        conference.conferenceId,
        conference.conferenceName,
        conference.creator,
        conference.visible
    )
}
