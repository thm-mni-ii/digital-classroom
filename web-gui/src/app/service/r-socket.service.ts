import {Injectable, OnDestroy} from '@angular/core';
import RSocketWebSocketClient from 'rsocket-websocket-client';
import {Subject} from "rxjs";
import {
  BEARER,
  BufferEncoders,
  encodeBearerAuthMetadata,
  encodeCompositeMetadata,
  encodeRoute,
  encodeWellKnownAuthMetadata,
  IdentitySerializer,
  JsonSerializer,
  MESSAGE_RSOCKET_AUTHENTICATION,
  MESSAGE_RSOCKET_COMPOSITE_METADATA,
  MESSAGE_RSOCKET_ROUTING,
  RSocketClient,
  TEXT_PLAIN,
  WellKnownAuthType
} from "rsocket-core";

import {AuthService} from "./auth.service";

@Injectable({
  providedIn: 'root'
})
export class RSocketService implements OnDestroy {

  title = 'client';
  client: RSocketClient<Buffer, Buffer>;
  sub = new Subject();

  constructor(auth: AuthService) {
    // Create an instance of a client
    this.client = new RSocketClient<Buffer, Buffer>({
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
            [TEXT_PLAIN, Buffer.from('Hello World')],
            [MESSAGE_RSOCKET_ROUTING, encodeRoute("stream/users")],
            [MESSAGE_RSOCKET_AUTHENTICATION, encodeBearerAuthMetadata(auth.loadToken()) ],
            ['custom/test/metadata', Buffer.from([1, 2, 3])],
          ]),
        },
      },
      transport: new RSocketWebSocketClient({
        url: 'wss://localhost:8085/rsocket', //window.origin.replace(/^http(s)?/, 'ws$1') + '/rsocket',
        debug: true,
        wsCreator: url => new WebSocket(url),
      }, BufferEncoders),
    });

    // Open the connection
    this.client.connect().subscribe({
      onComplete: (socket) => {
        // socket provides the rsocket interactions fire/forget, request/response,
        // request/stream, etc as well as methods to close the socket.
        socket
          .requestStream({
            data: undefined,
            metadata: encodeCompositeMetadata([
              [TEXT_PLAIN, Buffer.from('Hello World')],
              [MESSAGE_RSOCKET_ROUTING, encodeRoute("stream/users")],
              [MESSAGE_RSOCKET_AUTHENTICATION, encodeBearerAuthMetadata(auth.loadToken()) ],
              ['custom/test/metadata', Buffer.from([1, 2, 3])],
            ]),
          })
          .subscribe({
            onComplete: () => console.log('complete'),
            onError: error => {
              console.log('Connection has been closed due to:: ' + error);
              console.log(error.message)
              console.log(error.stack)
            },
            onNext: payload => {
              console.log(payload);
              this.addMessage(payload.data);
            },
            onSubscribe: subscription => {
              subscription.request(1000000);
            },
          });
      },
      onError: error => {
        console.log('Connection has been refused due to:: ' + error);
      },
      onSubscribe: cancel => {
        /* call cancel() to abort */
      }
    });
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
