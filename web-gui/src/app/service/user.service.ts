import {Injectable} from '@angular/core';
import {UserCredentials, User} from "../model/User";
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

  private users: Map<string, User> = new Map<string, User>()
  private offlineUsers: Map<string, User> = new Map<string, User>()
  // @ts-ignore
  private userSubject: Subject<User[]> = new BehaviorSubject([])
  userObservable: Observable<User[]> = this.userSubject.asObservable()

  private selfSubject: Subject<User> = new ReplaySubject(1)
  currentUserObservable: Observable<User> = this.selfSubject.asObservable()
  private currentUser?: User

  constructor(
    private rSocketService: RSocketService,
    private eventListenerService: EventListenerService,
    private authService: AuthService,
    private conferenceService: ConferenceService,
  ) {
    this.initUsers()
    this.initOfflineUsers()
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
            .filter(conference => conference.attendeeIds.includes(userId))
          map.set(userId, user)
        })
      })
    ).subscribe()
  }

  private initUsers() {
    this.rSocketService.requestStream<User>("socket/init-users", "Init Users").pipe(
      tap(users => {
        if (users.userId === this.authService.getToken().userId) {
          this.selfSubject.next(users)
        }
      }),
      tap(user => {
        this.users.set(user.userId, user)
      }),
      finalize(() => this.publish())
    ).subscribe()
  }

  private initOfflineUsers() {
    this.rSocketService.requestStream<User>("socket/init-offline-users", "Init Offline Users").pipe(
      tap(user => {
        this.offlineUsers.set(user.userId, user)
      })
    ).subscribe()
  }

  private handleUserEvent(userEvent: UserEvent) {
    if (userEvent.user === undefined) throw new Error("Received userEvent without user!")
    if (userEvent.userAction === undefined) throw new Error("Received userEvent without action!")

    switch (userEvent.userAction) {
      case UserAction.JOIN: {
        this.offlineUsers.delete(userEvent.user.userId)
        this.updateUser(userEvent.user);
        break;
      }
      case UserAction.LEAVE: {
        this.users.delete(userEvent.user.userId);
        this.offlineUsers.set(userEvent.user.userId, userEvent.user)
        break;
      }
      case UserAction.VISIBILITY_CHANGE: {
        this.updateVisibility(userEvent.user, userEvent.user.visible);
        break;
      }
    }
  }

  private updateUser(userDisplay: User) {
    if (userDisplay.userId === this.authService.getToken().userId) {
      this.selfSubject.next(userDisplay)
    }
    this.users.set(userDisplay.userId, userDisplay)
  }

  private updateVisibility(userCredentials: UserCredentials, visible: boolean) {
    const user = this.users.get(userCredentials.userId)
    if (user === undefined) throw new Error("User not found!")
    user.visible = visible
    this.users.set(user.userId, user)
  }

  private publish() {
    const users: User[] = []
    this.users.forEach((user) => {
      users.push(user)
    })
    this.userSubject.next(users)
  }

  public changeVisibility(visible: boolean) {
    if (this.currentUser === undefined) throw new Error("Current user is undefined!")
    const currentUser: User = this.currentUser;
    if (visible === currentUser.visible) return
    currentUser.visible = visible;
    this.selfSubject.next(currentUser);
    const event = new UserEvent();
    event.user = currentUser;
    event.userAction = UserAction.VISIBILITY_CHANGE;
    this.rSocketService.fireAndForget("socket/classroom-event", event)
  }

  public getFullUser(userId: string): User | undefined {
    let user: User | undefined = this.users.get(userId)
    if (user === undefined) user = this.offlineUsers.get(userId)
    return user
  }
}
