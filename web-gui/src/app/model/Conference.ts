import {User} from "./User";

export class Conference {
  conferenceId: string;
  classroomId: string
  conferenceName: string
  attendeePassword: string
  moderatorPassword: string
  creator: User
  visible: boolean;
  attendees: string[];
}
