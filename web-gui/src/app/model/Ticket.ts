import {User} from "./User";
import {ClassroomDependent} from "../rsocket/event/ClassroomEvent";

export interface Ticket extends ClassroomDependent {
  ticketId: number;
  description: string
  creator: User
  assignee?: User
  createTime: number
  classroomId: string
}
