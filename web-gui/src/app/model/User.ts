import {ClassroomDependent} from "../rsocket/event/ClassroomEvent";
import {ConferenceInfo} from "./ConferenceInfo";

export interface UserCredentials extends ClassroomDependent {
  classroomId: string;
  userId: string;
  fullName: string;
  userRole: UserRole; // STUDENT, TUTOR or TEACHER
}

export class User implements UserCredentials {
  classroomId: string;
  userId: string;
  fullName: string;
  userRole: UserRole;
  visible: boolean;
  conferences: ConferenceInfo[];
  avatarUrl: string;

  constructor(classroomId: string,
              userId: string,
              fullName: string,
              userRole: UserRole,
              visible: boolean,
              conferences: ConferenceInfo[],
              avatarUrl: string) {
    this.classroomId = classroomId;
    this.userId = userId;
    this.fullName = fullName;
    this.userRole = userRole;
    this.visible = visible;
    this.conferences = conferences;
    this.avatarUrl = avatarUrl
  }
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
