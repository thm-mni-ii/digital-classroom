import {UserCredentials} from "../../model/User";
import {ClassroomEvent} from "./ClassroomEvent";

export class UserEvent extends ClassroomEvent {
  user: UserCredentials
  visible: boolean
  userAction: UserAction

  constructor() {
      super("UserEvent");
  }
}

export enum UserAction {
  JOIN = "JOIN",
  LEAVE = "LEAVE",
  VISIBILITY_CHANGE = "VISIBILITY_CHANGE",
}
