import {ClassroomDependent} from "../rsocket/event/ClassroomEvent";
import {UserEvent} from "../rsocket/event/UserEvent";
import {ConferenceInfo} from "./ConferenceInfo";

export interface User extends ClassroomDependent {
  classroomId: string;
  userId: string;
  fullName: string;
  userRole?: UserRole; // STUDENT, TUTOR or TEACHER
  visible: boolean;
}

export class UserDisplay implements User {
  classroomId: string;
  userId: string;
  fullName: string;
  userRole: UserRole;
  visible: boolean;
  conferences: ConferenceInfo[];

  constructor(classroomId: string, userId: string, fullName: string, userRole: UserRole, visible: boolean, conferences: ConferenceInfo[]) {
    this.classroomId = classroomId;
    this.userId = userId;
    this.fullName = fullName;
    this.userRole = userRole;
    this.visible = visible;
    this.conferences = conferences;
  }
}

export function userDisplayFromEvent(userEvent: UserEvent) {
  return new UserDisplay(
    userEvent.user.classroomId,
    userEvent.user.userId,
    userEvent.user.fullName,
    userEvent.user.userRole,
    userEvent.user.visible,
    []
  )
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
