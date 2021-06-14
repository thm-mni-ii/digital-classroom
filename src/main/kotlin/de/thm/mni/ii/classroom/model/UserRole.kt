package de.thm.mni.ii.classroom.model

import org.springframework.security.core.GrantedAuthority

enum class UserRole(val value: Int, val bbbRole: BBBRole) : GrantedAuthority {
    Teacher(0, BBBRole.Moderator) {
            override fun getAuthority() = "TEACHER"
    },
    Tutor(1, BBBRole.Moderator) {
        override fun getAuthority() = "TUTOR"
    },
    Student(2, BBBRole.Participant) {
        override fun getAuthority() = "STUDENT"
    }
}

enum class BBBRole(val value: Int) {
    Moderator(0),
    Participant(1)
}
