export interface User {
  id?: string;
  fullName: string;
  classroomId: string;
  userRole?: string; // STUDENT, TUTOR or TEACHER
}
