package de.thm.mni.ii.classroom.model.classroom

import java.net.URL

open class ClassroomInfo(
    val classroomId: String,
    val classroomName: String,
    val logoutUrl: URL?,
)
