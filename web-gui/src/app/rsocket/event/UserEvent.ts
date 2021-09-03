import {User} from "../../model/User";
import {ClassroomEvent} from "./ClassroomEvent";

export class UserEvent extends ClassroomEvent {
  user: User
  inConference: boolean
  conferenceId: string
  userAction: UserAction

  constructor() {
      super("UserEvent");
  }

}

export enum UserAction {
  JOIN= "JOIN",
  JOIN_CONFERENCE = "JOIN_CONFERENCE",
  LEAVE_CONFERENCE = "LEAVE_CONFERENCE",
  LEAVE = "LEAVE"
}
