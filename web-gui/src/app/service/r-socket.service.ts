import {Injectable, OnDestroy} from '@angular/core';
import RSocketWebSocketClient from 'rsocket-websocket-client';
import {Subject} from "rxjs";
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

import {AuthService} from "./auth.service";

@Injectable({
  providedIn: 'root'
})
export class RSocketService implements OnDestroy {

  title = 'client';
  client: RSocketClient<Buffer, Buffer>;
  sub = new Subject();
  socket: any
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
      },
      transport: this.transport,
    });

    // Open the connection
    this.client.connect().subscribe({
      onComplete: (socket) => {
        // socket provides the rsocket interactions fire/forget, request/response,
        // request/stream, etc as well as methods to close the socket.
        this.socket = socket
        socket
          .requestStream({
            data: undefined,
            metadata: encodeCompositeMetadata([
              [MESSAGE_RSOCKET_ROUTING, encodeRoute("socket/classroom")],
              [MESSAGE_RSOCKET_AUTHENTICATION, encodeBearerAuthMetadata(auth.loadToken()) ],
            ]),
          })
          .subscribe({
            onComplete: () => {console.log("disconnected")},
            onError: error => {
              console.log('Connection has been closed due to:: ' + error);
              console.log(error.message)
              console.log(error.stack)
            },
            onSubscribe: subscription => {
              subscription.request(10000)
            },
            onNext: payload => console.log(payload)
          });
        this.fireEvent()
      },
      onError: error => {
        console.log('Connection has been refused due to:: ' + error);
      },
      onSubscribe: cancel => {}
    });
  }

  fireEvent() {
    this.socket.fireAndForget({
      data: toBuffer("Hallo Welt!"),
      metadata: encodeCompositeMetadata([
        [MESSAGE_RSOCKET_ROUTING, encodeRoute("socket/client")],
        [MESSAGE_RSOCKET_AUTHENTICATION, encodeBearerAuthMetadata(this.auth.loadToken()) ],
      ]),
    })
  }

  // tslint:disable-next-line:typedef
  addMessage(newMessage: any) {
    console.log('add message:' + JSON.stringify(newMessage))
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
    if (this.client) {
      this.client.close();
    }
  }

}
