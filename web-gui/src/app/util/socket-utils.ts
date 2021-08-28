import {Observable, Subject} from "rxjs";
import {Flowable, Single} from "rsocket-flowable";
import {
  encodeBearerAuthMetadata,
  encodeCompositeMetadata,
  encodeRoute,
  MESSAGE_RSOCKET_AUTHENTICATION,
  MESSAGE_RSOCKET_ROUTING,
  toBuffer
} from "rsocket-core";

export function singleToObservable<T>(single: Single<T>): Observable<T> {
  const subject = new Subject<T>()
  single.subscribe({
    onComplete: next => { subject.next(next) },
    onError: error => { subject.error(error) },
    onSubscribe: _ => { },
  });
  return subject
}

export function flowableToObservable<T>(flowable: Flowable<T>): Observable<T> {
    return new Observable<T>(subscriber => {
        flowable.subscribe(payload => {
          subscriber.next(payload)
        });
      }
    )
  /*
  return new Observable<T>(subscriber => {
      flowable.subscribe({
        onNext: next => { subscriber.next(next) },
        onError: error => { subscriber.error(error) },
        onSubscribe: _ => { },
        onComplete: () => subscriber.complete()
      });
    }
  )*/
}

export function decodeToString(buffer: Buffer): string {
  return new TextDecoder('utf-8').decode(buffer);
}

export function encodeData(data: any): Buffer {
  return toBuffer(JSON.stringify(data));
}

export function createMetadata(route: string, auth: string): Buffer {
  return encodeCompositeMetadata([
    [MESSAGE_RSOCKET_ROUTING, encodeRoute(route)],
    [MESSAGE_RSOCKET_AUTHENTICATION, encodeBearerAuthMetadata(auth) ],
  ])
}
