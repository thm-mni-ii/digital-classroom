import {Flowable, Single } from "rsocket-flowable";
import {Payload, Responder} from "rsocket-types";
import {Injectable} from "@angular/core";
import {Observable, ReplaySubject, Subject} from "rxjs";
import {TicketEvent} from "./event/TicketEvent";
import {decodeToString} from "../util/socket-utils";
import {ClassroomChangeEvent, ClassroomEvent} from "./event/ClassroomEvent";
import {UserEvent} from "./event/UserEvent";
import {ConferenceEvent} from "./event/ConferenceEvent";
import {InvitationEvent} from "./event/InvitationEvent";

@Injectable({
  providedIn: 'root'
})
export class EventListenerService implements Responder<Buffer, Buffer> {

  private ticketEventSubject: Subject<TicketEvent> = new ReplaySubject(1);
  ticketEvents: Observable<TicketEvent> = this.ticketEventSubject.asObservable();

  private userEventSubject: Subject<UserEvent> = new ReplaySubject(1);
  userEvents: Observable<UserEvent> = this.userEventSubject.asObservable();

  private conferenceEventSubject: Subject<ConferenceEvent> = new ReplaySubject(1);
  conferenceEvents: Observable<ConferenceEvent> = this.conferenceEventSubject.asObservable();

  private invitationEventSubject: Subject<InvitationEvent> = new ReplaySubject(1);
  invitationEvents: Observable<InvitationEvent> = this.invitationEventSubject.asObservable();

  private classroomChangeSubject: Subject<ClassroomChangeEvent> = new ReplaySubject(1);
  classroomChanges: Observable<ClassroomChangeEvent> = this.classroomChangeSubject.asObservable()

  fireAndForget(payload: Payload<Buffer, Buffer>): void {
    if (payload.data === undefined) throw new Error("Received message with empty payload data!")
    const event = (JSON.parse(decodeToString(payload.data)))
    switch ((event as ClassroomEvent).eventName) {
      case "TicketEvent": { this.ticketEventSubject.next(event as TicketEvent); break; }
      case "UserEvent": { this.userEventSubject.next(event as UserEvent); break; }
      case "ConferenceEvent": { this.conferenceEventSubject.next(event as ConferenceEvent); break; }
      case "InvitationEvent": { this.invitationEventSubject.next(event as InvitationEvent); break; }
      case "ClassroomChangeEvent": { this.classroomChangeSubject.next(event as ClassroomChangeEvent); break; }
      default: {
        console.log("Unknown event!\n" + event)
        break;
      }
    }
  }

  metadataPush(payload: Payload<Buffer, Buffer>): Single<void> {
    return Single.never();
  }

  requestChannel(payloads: Flowable<Payload<Buffer, Buffer>>): Flowable<Payload<Buffer, Buffer>> {
    return Flowable.never();
  }

  requestResponse(payload: Payload<Buffer, Buffer>): Single<Payload<Buffer, Buffer>> {
    return Single.never();
  }

  requestStream(payload: Payload<Buffer, Buffer>): Flowable<Payload<Buffer, Buffer>> {
    return Flowable.never();
  }

}
