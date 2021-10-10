import {User} from "../../model/User";
import {ClassroomEvent} from "./ClassroomEvent";

export class UserEvent extends ClassroomEvent {
  user: User
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
