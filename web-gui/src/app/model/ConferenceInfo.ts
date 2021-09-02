import {User} from "./User";
import {ClassroomDependent} from "../rsocket/event/ClassroomEvent";

export class ConferenceInfo implements ClassroomDependent {
  classroomId: string
  conferenceId: string
  conferenceName: string
  creator: User
  visible: boolean
  attendees: User[]
}

export class JoinLink {
  url: string
}
