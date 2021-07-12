import {User} from "./User";

export interface Ticket {
  description: string;
  status: string;
  createTime: number;
  assignee?: User;
  creator?: User;
  queuePosition?: number;
}
