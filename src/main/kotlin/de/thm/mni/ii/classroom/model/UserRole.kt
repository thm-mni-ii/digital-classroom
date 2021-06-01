package de.thm.mni.ii.classroom.model

enum class UserRole(val value: Int, val bbbRole: BBBRole) {
    Teacher(0, BBBRole.Moderator),
    Tutor(1, BBBRole.Moderator),
    Student(2, BBBRole.Participant)
}

enum class BBBRole(val value: Int) {
    Moderator(0),
    Participant(1)
}
