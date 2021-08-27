import {User} from "../../model/User";
import {Ticket} from "../../model/Ticket";
import {ConferenceInfo} from "../../model/Conference";

export interface ClassroomDependent {
  classroomId: string
}

export abstract class ClassroomEvent {
  private eventName: string

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

export class UserEvent extends ClassroomEvent {
  user: User
  joined: Boolean
  inConference: Boolean
  conferenceId: String

  constructor() {
    super("UserEvent");
  }

}

export class TicketEvent extends ClassroomEvent {
  ticket: Ticket
  open: Boolean

  constructor() {
    super("TicketEvent");
  }
}

export class ConferenceEvent extends ClassroomEvent {
  conferenceInfo: ConferenceInfo
  inProgress: Boolean

  constructor() {
    super("ConferenceEvent");
  }
}

export class InvitationEvent extends ClassroomEvent {
  inviter: User
  invitee: User
  conferenceInfo: ConferenceInfo
  joinLink: string

  constructor() {
    super("InvitationEvent");
  }
}
