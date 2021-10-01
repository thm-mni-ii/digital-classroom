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
import {ConnectionStatus, Payload, ReactiveSocket} from "rsocket-types";
import {
  createMetadata,
  decodeToString,
  encodeData, flowableToObservable, singleToObservable,
} from "../util/socket-utils";
import {first, map, switchMap, mergeMap} from "rxjs/operators";
import {EventListenerService} from "./event-listener.service";
import {environment} from "../../environments/environment";
import {NotificationService} from "../service/notification.service";

@Injectable({
  providedIn: 'root'
})
export class RSocketService implements OnDestroy {

  client: RSocketClient<Buffer, Buffer>;
  private socketSubject: ReplaySubject<ReactiveSocket<Buffer, Buffer>>
    = new ReplaySubject<ReactiveSocket<Buffer, Buffer>>(1)

  transport = new RSocketWebSocketClient({
    url: environment.wsUrl,
    debug: true
  }, BufferEncoders)

  private connectionStatus: ConnectionStatus

  constructor(private auth: AuthService,
              private notification: NotificationService,
              private responder: EventListenerService) {
    this.auth = auth
    const token = auth.loadToken()
    if (!auth.isAuthenticated()) {
      notification.showError("Sie sind nicht eingeloggt!")
      return
    }
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
            [MESSAGE_RSOCKET_AUTHENTICATION, encodeBearerAuthMetadata(token)],
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
        flowableToObservable(socket.connectionStatus()).subscribe(
          status => this.connectionStatus = status,
          )
      },
      onError: error => {
        notification.showError(error.message)
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
    return this.socketSubject.pipe(
      first<ReactiveSocket<Buffer, Buffer>>(),
      mergeMap((socket: ReactiveSocket<Buffer, Buffer>) => {
        return singleToObservable(socket.requestResponse({
          data: encodeData(data),
          metadata: createMetadata(route, this.auth.loadToken())
        }))
      }),
      map((payload: Payload<Buffer, Buffer>) => {
        return <T>JSON.parse(decodeToString(payload.data));
      })
    );
  }

  public requestStream<T>(route: string, data: any = ""): Observable<T> {
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
  }

  ngOnDestroy(): void {
    if (this.client) {
      this.client.close();
    }
  }

  isConnected(): boolean {
    if (this.connectionStatus === undefined) return false
    return this.connectionStatus.kind === "CONNECTED"
  }
}
