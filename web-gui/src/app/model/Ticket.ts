import {User} from "./User";

export interface Ticket {
  id: number;
  desc: string;
  courseId: number;
  status: string;
  timestamp: number;
  assignee: User;
  creator: User;
  queuePosition?: number;
}
