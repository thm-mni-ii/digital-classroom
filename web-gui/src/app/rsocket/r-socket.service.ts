import {Injectable, OnDestroy} from '@angular/core';
import RSocketWebSocketClient from 'rsocket-websocket-client';
import {Observable, ReplaySubject} from "rxjs";
import {
  BufferEncoders,
  encodeBearerAuthMetadata,
  encodeCompositeMetadata,
  encodeRoute,
  MESSAGE_RSOCKET_AUTHENTICATION,
  MESSAGE_RSOCKET_COMPOSITE_METADATA,
  MESSAGE_RSOCKET_ROUTING,
  RSocketClient
} from "rsocket-core";

import {AuthService} from "../service/auth.service";
import {ReactiveSocket, Payload} from "rsocket-types";
import {
  createMetadata,
  decodeToString,
  encodeData,
} from "../util/socket-utils";
import {first, map, switchMap} from "rxjs/operators";
import {EventListenerService} from "./event-listener.service";

@Injectable({
  providedIn: 'root'
})
export class RSocketService implements OnDestroy {

  client: RSocketClient<Buffer, Buffer>;
  private socketSubject: ReplaySubject<ReactiveSocket<Buffer, Buffer>>
    = new ReplaySubject<ReactiveSocket<Buffer, Buffer>>(1)
  auth: AuthService = undefined

  transport = new RSocketWebSocketClient({
    url: window.origin.replace(/^http(s)?/, 'ws$1') + '/rsocket',
    debug: true
  }, BufferEncoders)

  constructor(auth: AuthService, private responder: EventListenerService) {
    this.auth = auth
    // Create an instance of a client
    this.client = new RSocketClient({
      setup: {
        // ms btw sending keepalive to server
        keepAlive: 60000,
        // ms timeout if no keepalive response
        lifetime: 180000,
        // format of `data`
        dataMimeType: 'application/json',
        // format of `metadata`
        metadataMimeType: MESSAGE_RSOCKET_COMPOSITE_METADATA.string,
        payload: {
          data: undefined,
          metadata: encodeCompositeMetadata([
            [MESSAGE_RSOCKET_ROUTING, encodeRoute("")],
            [MESSAGE_RSOCKET_AUTHENTICATION, encodeBearerAuthMetadata(auth.loadToken())],
          ]),
        }
      },
      responder: responder,
      transport: this.transport,
    });

    // Open the connection
    this.client.connect().subscribe({
      onComplete: (socket) => {
        this.socketSubject.next(socket)
      },
      onError: error => {
        console.log('Connection has been refused due to:: ' + error);
      },
      onSubscribe: () => {}
    });
  }

  public fireAndForget<T>(route: string, data: any = "") {
    return this.socketSubject.pipe(
      first<ReactiveSocket<Buffer, Buffer>>(),
      map(socket => {
        return socket.fireAndForget({
          data: encodeData(data),
          metadata: createMetadata(route, this.auth.loadToken())
        })
      })
    ).subscribe();
  }

  public requestResponse<T>(route: string, data: any = ""): Observable<T> {
    const sub = new ReplaySubject<T>(1)
    this.socketSubject.subscribe(socket => {
      socket.requestResponse({
        data: encodeData(data),
        metadata: createMetadata(route, this.auth.loadToken())
      }).subscribe({
        onComplete: payload => {
          const obj = <T>JSON.parse(decodeToString(payload.data));
          sub.next(obj)
        },
        onError: error => sub.error(error)
      })
    })
    return sub;
  }

  public requestStream<T>(route: string, data: any = ""): Observable<T> {
    const sub = new ReplaySubject<T>(1)
    this.socketSubject.subscribe(socket => {
      socket.requestStream({
        data: encodeData(data),
        metadata: createMetadata(route, this.auth.loadToken())
      }).subscribe(payload => {
        const obj = <T>JSON.parse(decodeToString(payload.data));
        sub.next(obj)
      })
    })
    return sub;
    /*
    return this.socketSubject.pipe(
      first<ReactiveSocket<Buffer, Buffer>>(),
      switchMap((socket: ReactiveSocket<Buffer, Buffer>) => {
        return flowableToObservable(socket.requestStream({
          data: encodeData(data),
          metadata: createMetadata(route, this.auth.loadToken())
        }))
      }),
      map((payload: Payload<Buffer, Buffer>) => {
        return <T>JSON.parse(decodeToString(payload.data));
      })
    );
    */
  }

  ngOnDestroy(): void {
    if (this.client) {
      this.client.close();
    }
  }

  isConnected(): boolean {
    return true
  }
}
