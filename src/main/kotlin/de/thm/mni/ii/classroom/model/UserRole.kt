package de.thm.mni.ii.classroom.model

import org.springframework.security.core.GrantedAuthority

enum class UserRole(val value: Int, val bbbRole: BBBRole): GrantedAuthority {
    TEACHER(0, BBBRole.Moderator) {
            override fun getAuthority() = this.name
                                  },
    TUTOR(1, BBBRole.Moderator) {
        override fun getAuthority() = this.name
    },
    STUDENT(2, BBBRole.Participant) {
        override fun getAuthority() = this.name
    }
}

enum class BBBRole(val value: Int) {
    Moderator(0),
    Participant(1)
}
