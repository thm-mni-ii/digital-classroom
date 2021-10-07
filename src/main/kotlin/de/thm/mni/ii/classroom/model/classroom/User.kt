package de.thm.mni.ii.classroom.model.classroom

import com.fasterxml.jackson.annotation.JsonIgnore
import java.net.URL

class User(
    classroomId: String,
    userId: String,
    fullName: String,
    userRole: UserRole,
    var visible: Boolean,
    val conferences: List<ConferenceInfo>,
    val avatarUrl: URL?
) : UserCredentials(classroomId, userId, fullName, userRole) {
    constructor(userCredentials: UserCredentials, visible: Boolean, avatarUrl: String?) : this(
        userCredentials.classroomId,
        userCredentials.userId,
        userCredentials.fullName,
        userCredentials.userRole,
        visible,
        listOf(),
        if (!avatarUrl.isNullOrBlank()) URL(avatarUrl) else null
    )

    @JsonIgnore
    fun getCredentials(): UserCredentials = this
}
