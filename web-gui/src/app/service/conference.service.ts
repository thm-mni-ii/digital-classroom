import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable, ReplaySubject, Subject} from 'rxjs';
import {finalize, tap} from "rxjs/operators";
import {RSocketService} from "../rsocket/r-socket.service";
import {EventListenerService} from "../rsocket/event-listener.service";
import {ConferenceAction, ConferenceEvent, InvitationEvent} from "../rsocket/event/ClassroomEvent";
import {User, UserDisplay} from "../model/User";
import {ConferenceInfo, JoinLink} from "../model/ConferenceInfo";
import {UserService} from "./user.service";
import {Ticket} from "../model/Ticket";

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

  invitationEvents: Observable<InvitationEvent> = this.eventListenerService.invitationEvents;

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
    switch (conferenceEvent.conferenceAction) {
      case ConferenceAction.CREATE:  { this.conferences.set(conferenceEvent.conferenceInfo.conferenceId, conferenceEvent.conferenceInfo); break; }
      case ConferenceAction.CLOSE:   { this.conferences.delete(conferenceEvent.conferenceInfo.conferenceId); break; }
      case ConferenceAction.PUBLISH: { this.conferences.set(conferenceEvent.conferenceInfo.conferenceId, conferenceEvent.conferenceInfo); break; }
      case ConferenceAction.HIDE:    { this.conferences.set(conferenceEvent.conferenceInfo.conferenceId, conferenceEvent.conferenceInfo); break; }
    }
  }

  private publish() {
  }

  public createConference(conferenceName: string = 'Konferenz von ' + this.currentUser.fullName,
                          visible: boolean = true) {
    const conferenceInfo = new ConferenceInfo()
    conferenceInfo.classroomId = this.currentUser.classroomId
    conferenceInfo.creator = this.currentUser
    conferenceInfo.conferenceName = conferenceName
    conferenceInfo.visible = visible
    this.rSocketService.requestResponse<ConferenceInfo>("socket/conference/create", conferenceInfo).subscribe(conference => {
      this.currentConferenceSubject.next(conference)
    })
    this.currentConferenceObservable.subscribe(conf => {
      this.joinConference(conf)
    })
  }

  public joinConference(conference: ConferenceInfo) {
    if (this.conferenceWindowHandle == undefined || this.conferenceWindowHandle.closed) {
      this.rSocketService.requestResponse<JoinLink>("socket/conference/join", conference).subscribe( joinLink => {
        this.openConferenceWindow(joinLink)
      })
  } else {
      this.conferenceWindowHandle.focus()
    }
  }

  public joinConferenceOfUser(conferencingUser: User) {
      this.rSocketService.requestResponse<JoinLink>("socket/conference/join-user", conferencingUser).subscribe( joinLink => {
      this.openConferenceWindow(joinLink)
    })
  }

  private openConferenceWindow(joinLink: JoinLink) {
    this.conferenceWindowHandle = open(joinLink.url)
    this.conferenceWindowHandle.addEventListener("close", () => this.closeConference())
    this.conferenceWindowOpen = true
  }

  public closeConference(conference: ConferenceInfo = this.conferenceInfo) {
    console.log("conference closed!")
    this.rSocketService.fireAndForget("socket/conference/end", conference)
  }

  public inviteToConference(invitee: User, ticket: Ticket) {
    if (ticket === null) {
      this.createConference()
    } else {
      this.createConference(ticket.description)
    }
    this.sendInvitation(invitee)
  }

  private sendInvitation(invitee: User) {
    const invitationEvent = new InvitationEvent()
    invitationEvent.inviter = this.currentUser
    invitationEvent.invitee = invitee
    this.currentConferenceSubject.subscribe(conferenceInfo => {
      invitationEvent.conferenceInfo = conferenceInfo
      this.rSocketService.fireAndForget("socket/conference/invite", invitationEvent)
    })
  }
}
