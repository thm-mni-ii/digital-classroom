import {Injectable} from '@angular/core';
import {User} from "../model/User";
import {RSocketService} from "../rsocket/r-socket.service";
import {BehaviorSubject, Observable, Subject} from "rxjs";
import {EventListenerService} from "../rsocket/event-listener.service";
import {concatAll, filter, finalize, map} from "rxjs/operators";
import {UserAction, UserEvent} from "../rsocket/event/UserEvent";

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private users: Map<string, User> = new Map<string, User>()
  private userSubject: Subject<User[]> = new BehaviorSubject([])
  userObservable: Observable<User[]> = this.userSubject.asObservable()

  constructor(
    private rSocketService: RSocketService,
    private eventListenerService: EventListenerService
  ) {
    this.initUsers()
    this.eventListenerService.userEvents.pipe(
      map((userEvent: UserEvent) => this.handleUserEvent(userEvent)),
      map(() => this.publish())
    ).subscribe()
  }

  private initUsers() {
    this.rSocketService.requestStream<User>("socket/init-users", "Init Users").pipe(
      // Filter users already in this.users (currentUser)
      map(user => this.users.set(user.userId, user)),
      finalize(() => this.publish())
    ).subscribe()
  }

  private handleUserEvent(userEvent: UserEvent) {
    switch (userEvent.userAction) {
      case UserAction.JOIN: {
        this.users.set(userEvent.user.userId, userEvent.user)
        break;
      }
      case UserAction.LEAVE: {
        this.users.delete(userEvent.user.userId)
        break;
      }
      case UserAction.JOIN_CONFERENCE:
      case UserAction.LEAVE_CONFERENCE: {
        this.users.set(userEvent.user.userId, userEvent.user)
        break;
      }
    }
  }

  private publish() {
    const users: User[] = []
    this.users.forEach((user) => {
      users.push(user)
    })
    this.userSubject.next(users)
  }

}
