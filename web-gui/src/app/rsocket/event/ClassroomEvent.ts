export interface ClassroomDependent {
  classroomId: string | undefined
}

export abstract class ClassroomEvent {
  eventName: string

  protected constructor(eventName: string) {
    this.eventName = eventName
  }
}
