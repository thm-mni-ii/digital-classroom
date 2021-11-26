import {ClassroomInfo} from "../../model/ClassroomInfo";

export interface ClassroomDependent {
  classroomId: string | undefined
}

export abstract class ClassroomEvent {
  eventName: string

  protected constructor(eventName: string) {
    this.eventName = eventName
  }
}

export class ClassroomChangeEvent extends ClassroomEvent {
  classroomInfo?: ClassroomInfo

  constructor() {
    super("InvitationEvent");
  }
}
