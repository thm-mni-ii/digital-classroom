import {Injectable} from '@angular/core';
import {User} from "../model/User";
import {RSocketService} from "../rsocket/r-socket.service";
import {BehaviorSubject, Observable, Subject} from "rxjs";
import {EventListenerService} from "../rsocket/event-listener.service";
import {map} from "rxjs/operators";
import {UserAction, UserEvent} from "../rsocket/event/UserEvent";

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private users: User[] = []
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
      map(user => this.users.push(user)),
      map(() => this.publish())
    ).subscribe()
  }

  private handleUserEvent(userEvent: UserEvent) {
    switch (userEvent.userAction) {
      case UserAction.JOIN: {
        this.users.push(userEvent.user)
        break;
      }
      case UserAction.LEAVE: {
        const index = this.users.map(user => user.userId).indexOf(userEvent.user.userId)
        this.users.splice(index, 1)
        break;
      }
      case UserAction.JOIN_CONFERENCE:
      case UserAction.LEAVE_CONFERENCE: {
        const index = this.users.map(user => user.userId).indexOf(userEvent.user.userId)
        this.users[index] = userEvent.user
        break;
      }
    }
  }

  private publish() {
    this.userSubject.next(this.users)
  }

}
