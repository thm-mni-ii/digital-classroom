import {UserCredentials} from "./User";
import {ClassroomDependent} from "../rsocket/event/ClassroomEvent";

export class ConferenceInfo implements ClassroomDependent {
  classroomId: string = ""
  conferenceId: string = ""
  conferenceName: string = ""
  creator: UserCredentials | undefined
  visible: boolean = true
  creationTimestamp: number = Date.now()
  attendees: string[] = []
}

export class JoinLink {
  conference: ConferenceInfo | undefined
  url: string = ""
}
