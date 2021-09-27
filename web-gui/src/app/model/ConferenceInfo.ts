import {User} from "./User";
import {ClassroomDependent} from "../rsocket/event/ClassroomEvent";

export class ConferenceInfo implements ClassroomDependent {
  classroomId: string
  conferenceId: string
  conferenceName: string
  creator: User
  visible: boolean
  creationTimestamp: number = Date.now()
  attendees: string[] = []
}

export class JoinLink {
  conference: ConferenceInfo
  url: string
}
