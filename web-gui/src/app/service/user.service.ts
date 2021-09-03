import {Injectable} from '@angular/core';
import {UserDisplay, userDisplayFromEvent} from "../model/User";
import {RSocketService} from "../rsocket/r-socket.service";
import {BehaviorSubject, Observable, ReplaySubject, Subject} from "rxjs";
import {EventListenerService} from "../rsocket/event-listener.service";
import {finalize, map, tap} from "rxjs/operators";
import {UserAction, UserEvent} from "../rsocket/event/UserEvent";
import {AuthService} from "./auth.service";

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private users: Map<string, UserDisplay> = new Map<string, UserDisplay>()
  private userSubject: Subject<UserDisplay[]> = new BehaviorSubject([])
  userObservable: Observable<UserDisplay[]> = this.userSubject.asObservable()

  private selfSubject: Subject<UserDisplay> = new ReplaySubject(1)
  currentUserObservable: Observable<UserDisplay> = this.selfSubject.asObservable()

  constructor(
    private rSocketService: RSocketService,
    private eventListenerService: EventListenerService,
    private authService: AuthService
  ) {
    this.initUsers()
    this.eventListenerService.userEvents.pipe(
      tap((userEvent: UserEvent) => this.handleUserEvent(userEvent)),
      map(() => this.publish())
    ).subscribe()
  }

  private initUsers() {
    this.rSocketService.requestStream<UserDisplay>("socket/init-users", "Init Users").pipe(
      tap(userDisplay => {
        if (userDisplay.userId === this.authService.getToken().userId) {
          this.selfSubject.next(userDisplay)
        }
      }),
      tap(userDisplay => {
        this.users.set(userDisplay.userId, userDisplay)
      }),
      finalize(() => this.publish())
    ).subscribe()
  }

  private handleUserEvent(userEvent: UserEvent) {
    const userDisplay = userDisplayFromEvent(userEvent)
    switch (userEvent.userAction) {
      case UserAction.JOIN: { this.updateUser(userDisplay); break; }
      case UserAction.JOIN_CONFERENCE: { this.updateUser(userDisplay); break; }
      case UserAction.LEAVE_CONFERENCE: { this.updateUser(userDisplay); break; }

      case UserAction.LEAVE: {
        this.users.delete(userEvent.user.userId)
        break;
      }
    }
  }

  private updateUser(userDisplay: UserDisplay) {
    if (userDisplay.userId === this.authService.getToken().userId) {
      this.selfSubject.next(userDisplay)
    }
    this.users.set(userDisplay.userId, userDisplay)
  }

  private publish() {
    const users: UserDisplay[] = []
    this.users.forEach((userDisplay) => {
      users.push(userDisplay)
    })
    this.userSubject.next(users)
  }

}
