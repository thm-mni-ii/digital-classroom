import { Injectable } from '@angular/core';
import {EMPTY, Observable, Subject} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {WebSocketSubject} from "rxjs/internal-compatibility";
import {catchError, switchAll, tap} from "rxjs/operators";
import {webSocket} from "rxjs/webSocket";
import {AuthService} from "./auth.service";

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  constructor(private http: HttpClient,
              private authService: AuthService) { }

  private socket$: WebSocketSubject<any>;
  private messagesSubject$ = new Subject();
  public messages$ = this.messagesSubject$.pipe(switchAll(), catchError(e => { throw e }));

  public connect(): void {
    if (!this.socket$ || this.socket$.closed) {
      this.socket$ = this.getNewWebSocket();
      const messages = this.socket$.pipe(
        tap({
          error: error => console.log(error),
        }), catchError(_ => EMPTY));
      this.messagesSubject$.next(messages);
    }
  }

  private getNewWebSocket() {
    return webSocket(window.origin.replace(/^http(s)?/, 'ws$1') + '/websocket/users');
  }

  sendMessage(msg: any) {
    this.socket$.next(msg);
  }

  close() {
    this.socket$.complete();
  }

  private constructHeaders() {
    return {'Auth-Token': this.authService.loadToken()};
  }
}


