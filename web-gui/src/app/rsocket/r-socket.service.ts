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
  RSocketClient,
  toBuffer
} from "rsocket-core";

import {AuthService} from "../service/auth.service";
import {ReactiveSocket, Payload} from "rsocket-types";
import {flowableToObservable, singleToObservable} from "../util/FlowableAdapter";
import {first, map} from "rxjs/operators";
import {flatMap} from "rxjs/internal/operators";
import {Flowable, Single} from "rsocket-flowable";

@Injectable({
  providedIn: 'root'
})
export class RSocketService implements OnDestroy {

  title = 'client';
  client: RSocketClient<Buffer, Buffer>;
  private socketSubject: ReplaySubject<ReactiveSocket<Buffer, Buffer>>
    = new ReplaySubject<ReactiveSocket<Buffer, Buffer>>(1)
  auth: AuthService = undefined

  transport = new RSocketWebSocketClient({
    url: 'wss://localhost:8085/rsocket', //window.origin.replace(/^http(s)?/, 'ws$1') + '/rsocket',
    debug: true,
  }, BufferEncoders)

  constructor(auth: AuthService) {
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

  public requestResponse<T>(route: string, data: any): Observable<T> {
    return this.socketSubject.pipe(
      first<ReactiveSocket<Buffer, Buffer>>(),
      map((socket: ReactiveSocket<Buffer, Buffer>) => {
        return socket.requestResponse({
          data: this.encodeData(data),
          metadata: this.createMetadata(route)
        })
      }),
      flatMap((single: Single<Payload<Buffer, Buffer>>) => {
        return singleToObservable<Payload<Buffer, Buffer>>(single);
      }),
      map((payload: Payload<Buffer, Buffer>) => {
          return <T>JSON.parse(this.decodeToString(payload.data));
      })
    );
  }

  private decodeToString = (buffer: Buffer) => new TextDecoder('utf-8').decode(buffer);

  public requestStream<T>(route: string, data: any): Observable<T> {
    return this.socketSubject.pipe(
      first<ReactiveSocket<Buffer, Buffer>>(),
      map((socket: ReactiveSocket<Buffer, Buffer>) => {
        return socket.requestStream({
          data: this.encodeData(data),
          metadata: this.createMetadata(route)
        })
      }),
      flatMap((single: Flowable<Payload<Buffer, Buffer>>) => {
        return flowableToObservable<Payload<Buffer, Buffer>>(single);
      }),
      map((payload: Payload<Buffer, Buffer>) => {
        return <T>JSON.parse(this.decodeToString(payload.data));
      })
    );
  }

  private encodeData = (data: any): Buffer => toBuffer(JSON.stringify(data));

  private createMetadata(route: string): Buffer {
    return encodeCompositeMetadata([
      [MESSAGE_RSOCKET_ROUTING, encodeRoute(route)],
      [MESSAGE_RSOCKET_AUTHENTICATION, encodeBearerAuthMetadata(this.auth.loadToken()) ],
    ])
  }

  ngOnDestroy(): void {
    if (this.client) {
      this.client.close();
    }
  }

}
