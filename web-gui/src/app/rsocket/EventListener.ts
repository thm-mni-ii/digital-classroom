import {Flowable, Single } from "rsocket-flowable";
import {Payload, Responder} from "rsocket-types";
import {ClassroomEvent} from "./event/ClassroomEvent";


export class EventListener implements Responder<ClassroomEvent, Buffer> {

  fireAndForget(payload: Payload<ClassroomEvent, Buffer>): void {
    this.logRequest('fire and forget', payload)
  }

  metadataPush(payload: Payload<ClassroomEvent, Buffer>): Single<void> {
    return undefined;
  }

  requestChannel(payloads: Flowable<Payload<ClassroomEvent, Buffer>>): Flowable<Payload<ClassroomEvent, Buffer>> {
    return undefined;
  }

  requestResponse(payload: Payload<ClassroomEvent, Buffer>): Single<Payload<ClassroomEvent, Buffer>> {
    return undefined;
  }

  requestStream(payload: Payload<ClassroomEvent, Buffer>): Flowable<Payload<ClassroomEvent, Buffer>> {
    return undefined;
  }

  logRequest(type: string, payload: Payload<ClassroomEvent, Buffer>) {
    console.log(`Responder response to ${type}, data: ${JSON.stringify(payload.data) || 'null'}`);
  }

}
