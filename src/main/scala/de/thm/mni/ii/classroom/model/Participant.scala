package de.thm.mni.ii.classroom.model

case class Participant(user: User, role: CourseRole.Value, visible: Boolean = true)
