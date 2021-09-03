import {ClassroomDependent} from "../rsocket/event/ClassroomEvent";
import {UserEvent} from "../rsocket/event/UserEvent";

export interface User extends ClassroomDependent {
  classroomId: string;
  userId: string;
  fullName: string;
  userRole?: UserRole; // STUDENT, TUTOR or TEACHER
}

export class UserDisplay implements User {
  classroomId: string;
  userId: string;
  fullName: string;
  userRole: UserRole;
  inConference: boolean;
  conferenceId: string;

  constructor(classroomId: string, userId: string, fullName: string, userRole: UserRole, inConference: boolean, conferenceId: string) {
    this.classroomId = classroomId;
    this.userId = userId;
    this.fullName = fullName;
    this.userRole = userRole;
    this.inConference = inConference;
    this.conferenceId = conferenceId;
  }
}

export function userDisplayFromEvent(userEvent: UserEvent) {
  return new UserDisplay(
    userEvent.user.classroomId,
    userEvent.user.userId,
    userEvent.user.fullName,
    userEvent.user.userRole,
    userEvent.inConference,
    userEvent.conferenceId
  )
}

export function defaultUserDisplay(user: User) {
  return new UserDisplay(
    user.classroomId,
    user.userId,
    user.fullName,
    user.userRole,
    false,
    null
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
