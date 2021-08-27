package de.thm.mni.ii.classroom.model.classroom

import de.thm.mni.ii.classroom.model.ClassroomDependent

open class ConferenceInfo(
    override val classroomId: String,
    val conferenceId: String,
    val conferenceName: String,
    val creator: User,
    val visible: Boolean,
    val attendees: HashSet<User>,
): ClassroomDependent
