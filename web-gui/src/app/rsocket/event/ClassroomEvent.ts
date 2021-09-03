import {User} from "../../model/User";
import {ConferenceInfo} from "../../model/ConferenceInfo";

export interface ClassroomDependent {
  classroomId: string
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
  PUBLISH = "PUBLISH",
  HIDE = "HIDE"
}

export class ConferenceEvent extends ClassroomEvent {
  conferenceInfo: ConferenceInfo
  inProgress: Boolean
  conferenceAction: ConferenceAction

  constructor() {
    super("ConferenceEvent");
  }
}

export class InvitationEvent extends ClassroomEvent {
  inviter: User
  invitee: User
  conferenceInfo: ConferenceInfo

  constructor() {
    super("InvitationEvent");
  }
}
