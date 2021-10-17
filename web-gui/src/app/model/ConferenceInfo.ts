import {UserCredentials} from "./User";
import {ClassroomDependent} from "../rsocket/event/ClassroomEvent";

export class ConferenceInfo implements ClassroomDependent {
  classroomId: string = ""
  conferenceId: string | null = null
  conferenceName: string = ""
  creator?: UserCredentials
  visible: boolean = true
  creationTimestamp: number = Date.now()
  attendeeIds: string[] = []
  ticketId: number | null = null
}

export class JoinLink {
  conference: ConferenceInfo | undefined
  url: string = ""
}
