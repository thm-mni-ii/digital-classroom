import {Observable, Subject} from "rxjs";
import {Flowable, Single} from "rsocket-flowable";

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
      flowable.subscribe({
        onNext: next => { subscriber.next(next) },
        onError: error => { subscriber.error(error) },
        onSubscribe: _ => { },
        onComplete: () => subscriber.complete()
      });
    }
  )
}
