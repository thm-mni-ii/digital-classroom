import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable, ReplaySubject, Subject} from 'rxjs';
import {distinctUntilChanged, finalize, first, tap} from "rxjs/operators";
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

  private attendedConferences: Map<string, ConferenceInfo> = new Map<string, ConferenceInfo>()
  private attendedConferencesSubject: Subject<ConferenceInfo[]> = new ReplaySubject(1)
  attendedConferencesObservable: Observable<ConferenceInfo[]> = this.attendedConferencesSubject.asObservable()

  private conferenceWindowHandle: Map<string, Window> = new Map<string, Window>()
  private isConferenceWindowOpen: Subject<ConferenceOpenInfo[]>
  conferenceWindowOpen: boolean = false

  private currentUser: UserDisplay;

  constructor(
    private rSocketService: RSocketService,
    private eventListenerService: EventListenerService,
    private userService: UserService
  ) {
    this.initConferences()
    this.initWindowHandle()
    this.attendedConferencesObservable.subscribe(currentConferences => {
      currentConferences.forEach(conference => { this.attendedConferences.set(conference.conferenceId, conference) })
    })
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
      tap(conf => { this.conferences.set(conf.conferenceId, conf) }),
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
    this.conferenceSubject.next(Array.from(this.conferences.values()))
    this.attendedConferencesSubject.next(Array.from(this.attendedConferences.values()))
  }

  public createConference(conferenceName: string = 'Konferenz von ' + this.currentUser.fullName, visible: boolean = true) {
    const conferenceInfo = new ConferenceInfo()
    conferenceInfo.classroomId = this.currentUser.classroomId
    conferenceInfo.creator = this.currentUser
    conferenceInfo.conferenceName = conferenceName
    conferenceInfo.visible = visible
    this.rSocketService.requestResponse<ConferenceInfo>("socket/conference/create", conferenceInfo).pipe(
      tap(conference => this.conferences.set(conference.conferenceId, conference)),
      tap(conference => this.joinConference(conference)),
      tap(_ => this.publish()),
    ).subscribe()
  }

  public joinConference(conference: ConferenceInfo) {
    if (this.conferenceWindowHandle.size === 0 || !this.conferenceWindowHandle.has(conference.conferenceId)) {
      this.rSocketService.requestResponse<JoinLink>("socket/conference/join", conference).pipe(
        tap(joinLink => this.attendedConferences.set(joinLink.conference.conferenceId, joinLink.conference)),
        tap(joinLink => this.openConferenceWindow(joinLink)),
        tap(_ => this.publish())
      ).subscribe()
    } else {
      this.conferenceWindowHandle.get(conference.conferenceId).focus()
    }
  }

  public joinConferenceOfUser(conferencingUser: User) {
      this.rSocketService.requestResponse<JoinLink>("socket/conference/join-user", conferencingUser).pipe(
        tap(joinLink => this.attendedConferences.set(joinLink.conference.conferenceId, joinLink.conference)),
        tap(joinLink => this.openConferenceWindow(joinLink)),
        tap(_ => this.publish())
      ).subscribe()
  }

  private openConferenceWindow(joinLink: JoinLink) {
    const conference = joinLink.conference
    if (this.conferenceWindowHandle.has(conference.conferenceId)) {
      this.conferenceWindowHandle.get(conference.conferenceId).focus()
    } else {
      this.conferenceWindowHandle.set(conference.conferenceId, open(joinLink.url))
    }
    this.conferenceWindowOpen = true
  }

  public leaveConference(conference: ConferenceInfo = this.attendedConferences[0]) {
    console.log("conference left!")
    this.conferenceWindowOpen = false
    this.conferenceWindowHandle.delete(conference.conferenceId)
    this.rSocketService.fireAndForget("socket/conference/leave", conference)
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
    this.attendedConferencesObservable.subscribe(conferenceInfo => {
      invitationEvent.conferenceInfo = conferenceInfo[0]
      this.rSocketService.fireAndForget("socket/conference/invite", invitationEvent)
    })
  }

  private initWindowHandle() {
    this.isConferenceWindowOpen = new ReplaySubject<ConferenceOpenInfo[]>();
    this.isConferenceWindowOpen.asObservable().pipe(
      distinctUntilChanged(),
      tap(conferenceOpenInfos => {
        conferenceOpenInfos.forEach(conference => {
          if (conference.isClosed) {
            const conferenceInfo = this.attendedConferences.get(conference.conferenceId)
            this.leaveConference(conferenceInfo)
            this.attendedConferences.delete(conference.conferenceId)
            this.conferenceWindowHandle.delete(conference.conferenceId)
            this.publish()
          }
        })
      })
    ).subscribe()

    setInterval(() => {
      const conferenceOpenInfos: ConferenceOpenInfo[] = []
      if (this.conferenceWindowHandle.size > 0) {
        this.conferenceWindowHandle.forEach((window, conferenceId) => {
            conferenceOpenInfos.push(new ConferenceOpenInfo(conferenceId, window.closed))
        })
        this.isConferenceWindowOpen.next(conferenceOpenInfos)
      }
    }, 1000);
  }
}

class ConferenceOpenInfo {
  conferenceId: string
  isClosed: boolean

  constructor(conferenceId: string, isClosed: boolean) {
    this.conferenceId = conferenceId
    this.isClosed = isClosed
  }

}

