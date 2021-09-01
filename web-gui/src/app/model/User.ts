import {ClassroomDependent} from "../rsocket/event/ClassroomEvent";

export class User implements ClassroomDependent {
  classroomId: string;
  userId: string;
  fullName: string;
  userRole?: UserRole; // STUDENT, TUTOR or TEACHER
  inConference?: boolean;
  conferenceId?: string;
}

export enum UserRole {
  STUDENT = "STUDENT",
  TUTOR = "TUTOR",
  TEACHER = "TEACHER"
}

export function parseCourseRole(role: UserRole): String {
  switch (role) {
    case 'TEACHER': return 'Dozent';
    case 'TUTOR': return 'Tutor';
    case 'STUDENT': return 'Student';
    default: return "Student";
  }
}
