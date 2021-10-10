import {UserCredentials} from "./User";
import {ClassroomDependent} from "../rsocket/event/ClassroomEvent";

export class ConferenceInfo implements ClassroomDependent {
  classroomId: string
  conferenceId: string
  conferenceName: string
  creator: UserCredentials
  visible: boolean
  creationTimestamp: number = Date.now()
  attendees: string[] = []
}

export class JoinLink {
  conference: ConferenceInfo
  url: string
}
