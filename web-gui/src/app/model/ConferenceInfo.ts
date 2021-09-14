import {User} from "./User";
import {ClassroomDependent} from "../rsocket/event/ClassroomEvent";

export class ConferenceInfo implements ClassroomDependent {
  classroomId: string
  conferenceId: string
  conferenceName: string
  creator: User
  visible: boolean
  creation: number = Date.now()
}

export class JoinLink {
  conference: ConferenceInfo
  url: string
}
