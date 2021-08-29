import {User} from "./User";
import {ClassroomDependent} from "../rsocket/event/ClassroomEvent";

export class Ticket implements ClassroomDependent {
  classroomId: string
  ticketId: number;
  creator: User
  assignee?: User
  description: string
  createTime: number

  constructor(description: string) {
    this.classroomId = null
    this.ticketId = null
    this.creator = null
    this.assignee = null
    this.description = description
    this.createTime = Date.now()
  }
}
