import {UserCredentials} from "./User";
import {ClassroomDependent} from "../rsocket/event/ClassroomEvent";

export class Ticket implements ClassroomDependent {
  classroomId: string = "";
  ticketId: number = 0;
  creator?: UserCredentials;
  assignee?: UserCredentials;
  description: string = "";
  createTime: number = 0;

  constructor(description: string) {
    this.classroomId = ""
    this.ticketId = 0
    this.creator = undefined
    this.assignee = undefined
    this.description = description
    this.createTime = Date.now()
  }
}
