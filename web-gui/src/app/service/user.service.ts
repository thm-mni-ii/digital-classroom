import {Injectable} from '@angular/core';
import {User, UserDisplay, userDisplayFromEvent} from "../model/User";
import {RSocketService} from "../rsocket/r-socket.service";
import {BehaviorSubject, Observable, ReplaySubject, Subject} from "rxjs";
import {EventListenerService} from "../rsocket/event-listener.service";
import {distinctUntilChanged, finalize, map, tap} from "rxjs/operators";
import {UserAction, UserEvent} from "../rsocket/event/UserEvent";
import {AuthService} from "./auth.service";
import {ConferenceService} from "./conference.service";

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private users: Map<string, UserDisplay> = new Map<string, UserDisplay>()
  private userSubject: Subject<UserDisplay[]> = new BehaviorSubject([])
  userObservable: Observable<UserDisplay[]> = this.userSubject.asObservable()

  private selfSubject: Subject<UserDisplay> = new ReplaySubject(1)
  currentUserObservable: Observable<UserDisplay> = this.selfSubject.asObservable()
  private currentUser: UserDisplay

  constructor(
    private rSocketService: RSocketService,
    private eventListenerService: EventListenerService,
    private authService: AuthService,
    private conferenceService: ConferenceService,
  ) {
    this.initUsers()
    this.eventListenerService.userEvents.pipe(
      tap((userEvent: UserEvent) => this.handleUserEvent(userEvent)),
      map(() => this.publish())
    ).subscribe()
    this.currentUserObservable.subscribe(currentUser => this.currentUser = currentUser);
    this.conferenceService.conferencesObservable.pipe(
      distinctUntilChanged(),
      tap(conferences => {
        this.users.forEach((user, userId, map) => {
          user.conferences = conferences
            .filter(conference => conference.visible)
            .filter(conference => conference.attendees.includes(userId))
          map.set(userId, user)
        })
      })
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
    switch (userEvent.userAction) {
      case UserAction.JOIN: {
        this.updateUser(userDisplayFromEvent(userEvent));
        break;
      }
      case UserAction.LEAVE: {
        this.users.delete(userEvent.user.userId);
        break;
      }
      case UserAction.VISIBILITY_CHANGE: {
        this.updateVisibility(userEvent.user, userEvent.user.visible);
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

  private updateVisibility(user: User, visible: boolean) {
    const userDisplay = this.users.get(user.userId)
    userDisplay.visible = visible
    this.users.set(userDisplay.userId, userDisplay)
  }

  private publish() {
    const users: UserDisplay[] = []
    this.users.forEach((user) => {
      users.push(user)
    })
    this.userSubject.next(users)
  }

  public changeVisibility(visible: boolean) {
    const currentUser: UserDisplay = this.currentUser;
    if (visible === currentUser.visible) return
    currentUser.visible = visible;
    this.selfSubject.next(currentUser);
    const event = new UserEvent();
    event.user = currentUser;
    event.visible = visible;
    event.userAction = UserAction.VISIBILITY_CHANGE;
    this.rSocketService.fireAndForget("socket/classroom-event", event)
  }
}
