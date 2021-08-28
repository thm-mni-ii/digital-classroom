import {Flowable, Single } from "rsocket-flowable";
import {Payload, Responder} from "rsocket-types";
import {Injectable} from "@angular/core";
import {Observable, ReplaySubject, Subject} from "rxjs";
import {TicketEvent} from "./event/TicketEvent";
import {decodeToString} from "../util/socket-utils";
import {ClassroomEvent} from "./event/ClassroomEvent";

@Injectable({
  providedIn: 'root'
})
export class EventListenerService implements Responder<Buffer, Buffer> {

  private ticketEventSubject: Subject<TicketEvent> = new ReplaySubject(1)
  ticketEvents: Observable<TicketEvent> = this.ticketEventSubject.asObservable()

  fireAndForget(payload: Payload<Buffer, Buffer>): void {
    const event = (JSON.parse(decodeToString(payload.data)))
    switch ((event as ClassroomEvent).eventName) {
      case "TicketEvent": {
        this.ticketEventSubject.next(event as TicketEvent)
        break;
      }
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

  logRequest(type: string, payload: Payload<Buffer, Buffer>) {
    console.log(`Responder response to ${type}, data: ${JSON.stringify(payload.data) || 'null'}`);
  }

}
