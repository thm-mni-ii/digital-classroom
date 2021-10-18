import {UserCredentials} from "./User";
import {ClassroomDependent} from "../rsocket/event/ClassroomEvent";

export class Ticket implements ClassroomDependent {
  classroomId: string = "";
  ticketId: number = 0;
  creator: UserCredentials;
  assignee?: UserCredentials;
  description: string = "";
  createTime: number = 0;
  conferenceId: string | null = null;

  constructor(description: string, creator: UserCredentials) {
    this.classroomId = ""
    this.ticketId = 0
    this.creator = creator
    this.assignee = undefined
    this.description = description
    this.createTime = Date.now()
  }
}
