import {Injectable, OnDestroy} from '@angular/core';
import RSocketWebSocketClient from 'rsocket-websocket-client';
import {Subject} from "rxjs";
import {IdentitySerializer, JsonSerializer, RSocketClient} from "rsocket-core";

@Injectable({
  providedIn: 'root'
})
export class RSocketService implements OnDestroy {

  title = 'client';
  message = '';
  messages: any[];
  client: RSocketClient<any, any>;
  sub = new Subject();

  constructor() {
    this.messages = [];

    // Create an instance of a client
    this.client = new RSocketClient({
      serializers: {
        data: JsonSerializer,
        metadata: IdentitySerializer
      },
      setup: {
        // ms btw sending keepalive to server
        keepAlive: 60000,
        // ms timeout if no keepalive response
        lifetime: 180000,
        // format of `data`
        dataMimeType: 'application/json',
        // format of `metadata`
        metadataMimeType: 'message/x.rsocket.routing.v0',
      },
      transport: new RSocketWebSocketClient({
        url: 'wss://localhost:8085/rsocket', //window.origin.replace(/^http(s)?/, 'ws$1') + '/rsocket',
        debug: true
      }),
    });

    // Open the connection
    this.client.connect().subscribe({
      onComplete: (socket) => {

        // socket provides the rsocket interactions fire/forget, request/response,
        // request/stream, etc as well as methods to close the socket.
        socket
          .requestStream({
            data: null,
            metadata: String.fromCharCode('messages'.length) + 'messages'
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

        this.sub.subscribe({
          next: (data) => {
            socket.fireAndForget({
              data,
              metadata: String.fromCharCode('send'.length) + 'send',
            });
          }
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
    this.messages = [...this.messages, newMessage];
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
    if (this.client) {
      this.client.close();
    }
  }

  // tslint:disable-next-line:typedef
  sendMessage() {
    console.log('sending message:' + this.message);
    this.sub.next(this.message);
    this.message = '';
  }

}
