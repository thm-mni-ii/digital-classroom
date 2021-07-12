export interface User {
  userId: string;
  fullName: string;
  classroomId: string;
  userRole?: string; // STUDENT, TUTOR or TEACHER
}
