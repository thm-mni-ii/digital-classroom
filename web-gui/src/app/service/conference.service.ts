import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable, ReplaySubject, Subject} from 'rxjs';
import {finalize, tap} from "rxjs/operators";
import {RSocketService} from "../rsocket/r-socket.service";
import {EventListenerService} from "../rsocket/event-listener.service";
import {ConferenceEvent} from "../rsocket/event/ClassroomEvent";
import {User, UserDisplay} from "../model/User";
import {ConferenceInfo} from "../model/ConferenceInfo";
import {ClassroomService} from "./classroom.service";
import {UserService} from "./user.service";

/**
 * Handles the creation and retrivement of conference links.
 * @author Andrej Sajenko
 */
@Injectable({
  providedIn: 'root'
})
export class ConferenceService {

  private conferences: Map<string, ConferenceInfo> = new Map<string, ConferenceInfo>()
  private conferenceSubject: Subject<ConferenceInfo[]> = new BehaviorSubject([])
  conferencesObservable: Observable<ConferenceInfo[]> = this.conferenceSubject.asObservable()

  private currentConferenceSubject: Subject<ConferenceInfo> = new ReplaySubject(1)
  currentConferenceObservable: Observable<ConferenceInfo> = this.currentConferenceSubject.asObservable()

  private conferenceWindowHandle: Window;


  conferenceWindowOpen: boolean = false
  private conferenceInfo: ConferenceInfo;
  private currentUser: UserDisplay = undefined;

  constructor(
    private rSocketService: RSocketService,
    private eventListenerService: EventListenerService,
    private userService: UserService
  ) {
    this.initConferences()
    this.userService.currentUserObservable.subscribe(currentUser => {
      this.currentUser = currentUser
    })
    this.eventListenerService.conferenceEvents.pipe(
      tap((conferenceEvent: ConferenceEvent) => this.handleConferenceEvent(conferenceEvent)),
      tap(() => this.publish())
    ).subscribe()
  }

  private initConferences() {
    this.rSocketService.requestStream<ConferenceInfo>("socket/init-conferences", "Init Conferences").pipe(
      tap(conf => this.conferences.set(conf.conferenceId, conf)),
      finalize(() => this.publish())
    ).subscribe()
  }

  private handleConferenceEvent(conferenceEvent: ConferenceEvent) {
  }

  private publish() {
  }

  public createConference(visible: boolean = true) {
    const conferenceInfo = new ConferenceInfo()
    conferenceInfo.classroomId = this.currentUser.classroomId
    conferenceInfo.creator = this.currentUser
    conferenceInfo.conferenceName = "Test Konferenz!"
    conferenceInfo.visible = visible
    this.rSocketService.requestResponse<ConferenceInfo>("socket/conference/create", conferenceInfo).subscribe(conference => {
      this.currentConferenceSubject.next(conference)
    })
  }

  public joinConference(conference: ConferenceInfo) {
    this.rSocketService.requestResponse<string>("socket/conference/join", conference)
  }

  public joinConferenceOfUser(conferencingUser: User) {
    this.rSocketService.requestResponse<string>("socket/conference/join-user", conferencingUser)
  }

  public closeConference(conference: ConferenceInfo = this.conferenceInfo) {
    this.rSocketService.requestResponse<string>("socket/conference/close", conference)
  }

}
