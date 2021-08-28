import {ClassroomDependent} from "../rsocket/event/ClassroomEvent";
import {Roles} from "./Roles";

export class User implements ClassroomDependent {
  userId: string;
  fullName: string;
  classroomId: string;
  userRole?: string; // STUDENT, TUTOR or TEACHER

  isAuthorized() {
    return Roles.isPrivileged(this.userRole)
  }
}

export class UserDisplay extends User {
  inConference?: boolean;
  conferenceId?: string;
}
