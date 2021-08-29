import {ClassroomDependent} from "../rsocket/event/ClassroomEvent";
import {Roles} from "./Roles";

export class User implements ClassroomDependent {
  classroomId: string;
  userId: string;
  fullName: string;
  userRole?: string; // STUDENT, TUTOR or TEACHER
  inConference?: boolean;
  conferenceId?: string;

  public isAuthorized() {
    return Roles.isPrivileged(this.userRole)
  }
}
