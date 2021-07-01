package de.thm.mni.ii.classroom.downstream

enum class APIQueryParamTranslation(val api: String) {

    StudentPassword("attendeePW"),
    TutorPassword("tutorPW"),
    TeacherPassword("moderatorPW"),
    ClassroomName("name"),
    Password("password"),
    UserId("userID"),
    userName("fullName"),
    ClassroomId("meetingID")

}