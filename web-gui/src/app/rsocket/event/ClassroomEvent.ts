import {UserCredentials} from "../../model/User";
import {ConferenceInfo} from "../../model/ConferenceInfo";

export interface ClassroomDependent {
  classroomId: string | undefined
}

export abstract class ClassroomEvent {
  eventName: string

  protected constructor(eventName: string) {
    this.eventName = eventName
  }
}

export class MessageEvent extends ClassroomEvent {
  message: string

  constructor(message: string) {
    super("MessageEvent")
    this.message = message
  }
}

export enum ConferenceAction {
  CREATE = "CREATE",
  CLOSE = "CLOSE",
  VISIBILITY = "VISIBILITY",
  USER_CHANGE = "USER_CHANGE"
}

export class ConferenceEvent extends ClassroomEvent {
  conferenceInfo: ConferenceInfo | undefined
  conferenceAction: ConferenceAction | undefined

  constructor() {
    super("ConferenceEvent");
  }
}

export class InvitationEvent extends ClassroomEvent {
  inviter: UserCredentials | undefined
  invitee: UserCredentials | undefined
  conferenceInfo: ConferenceInfo | undefined

  constructor() {
    super("InvitationEvent");
  }
}
