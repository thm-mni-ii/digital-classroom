package de.thm.mni.ii.classroom.util

enum class BbbApiQueryParamKeys(val key: String) {

    StudentPassword("attendeePW"),
    TutorPassword("tutorPW"),
    TeacherPassword("moderatorPW"),
    ClassroomName("name"),
    Password("password"),
    UserId("userID"),
    Username("fullName"),
    ClassroomId("meetingID"),
    LogoutUrl("logoutURL")
}
