import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable, Subject} from 'rxjs';
import {
  finalize,
  tap
} from "rxjs/operators";
import {RSocketService} from "../rsocket/r-socket.service";
import {EventListenerService} from "../rsocket/event-listener.service";
import {ConferenceAction, ConferenceEvent, InvitationEvent} from "../rsocket/event/ClassroomEvent";
import {UserCredentials} from "../model/User";
import {ConferenceInfo, JoinLink} from "../model/ConferenceInfo";

@Injectable({
  providedIn: 'root'
})
export class ConferenceService {

  private conferences: Map<string, ConferenceInfo> = new Map<string, ConferenceInfo>()
  private conferenceSubject: Subject<ConferenceInfo[]> = new BehaviorSubject([])
  conferencesObservable: Observable<ConferenceInfo[]> = this.conferenceSubject.asObservable()

  invitationEvents: Observable<InvitationEvent> = this.eventListenerService.invitationEvents;

  private conferenceWindowHandles: Map<string, Window> = new Map<string, Window>()
  private conferenceClosedSubject: Subject<ConferenceOpenInfo>

  public isCurrentUserInConference(): boolean {
    return this.conferenceWindowHandles.size !== 0
  }

  constructor(
    private rSocketService: RSocketService,
    private eventListenerService: EventListenerService
  ) {
    this.initConferences()
    this.initWindowHandle()
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
      case ConferenceAction.CREATE:      { this.conferences.set(conferenceEvent.conferenceInfo.conferenceId, conferenceEvent.conferenceInfo); break; }
      case ConferenceAction.CLOSE:       { this.conferences.delete(conferenceEvent.conferenceInfo.conferenceId); break; }
      case ConferenceAction.PUBLISH:     { this.conferences.set(conferenceEvent.conferenceInfo.conferenceId, conferenceEvent.conferenceInfo); break; }
      case ConferenceAction.HIDE:        { this.conferences.set(conferenceEvent.conferenceInfo.conferenceId, conferenceEvent.conferenceInfo); break; }
      case ConferenceAction.USER_CHANGE: { this.conferences.set(conferenceEvent.conferenceInfo.conferenceId, conferenceEvent.conferenceInfo); break; }
    }
  }

  private publish() {
    this.conferenceSubject.next(Array.from(this.conferences.values()))
  }

  public createConference(conferenceInfo: ConferenceInfo): Observable<ConferenceInfo> {
    const subject = new Subject<ConferenceInfo>()
    this.rSocketService.requestResponse<ConferenceInfo>("socket/conference/create", conferenceInfo).pipe(
      tap(conference => this.conferences.set(conference.conferenceId, conference)),
      tap(conference => this.joinConference(conference)),
      tap(_ => this.publish()),
      tap(conference => {
        subject.next(conference)
        subject.complete()
      })).subscribe()
    return subject
  }

  public joinConference(conference: ConferenceInfo) {
    if (this.conferenceWindowHandles.size === 0 || !this.conferenceWindowHandles.has(conference.conferenceId)) {
      this.rSocketService.requestResponse<JoinLink>("socket/conference/join", conference).pipe(
        tap(joinLink => this.openConferenceWindow(joinLink)),
        tap(_ => this.publish())
      ).subscribe()
    } else {
      this.conferenceWindowHandles.get(conference.conferenceId).focus()
    }
  }

  public joinConferenceOfUser(conferencingUser: UserCredentials) {
      this.rSocketService.requestResponse<JoinLink>("socket/conference/join-user", conferencingUser).pipe(
        tap(joinLink => this.openConferenceWindow(joinLink)),
        tap(_ => this.publish())
      ).subscribe()
  }

  private openConferenceWindow(joinLink: JoinLink) {
    const conference = joinLink.conference
    if (this.conferenceWindowHandles.has(conference.conferenceId)) {
      this.conferenceWindowHandles.get(conference.conferenceId).focus()
    } else {
      const conferenceWindow = window.open(joinLink.url)
      if (conferenceWindow === null) {
        throw new Error("Window to conference " + joinLink.conference.conferenceName + " could not be opened!")
      }
      this.conferenceWindowHandles.set(conference.conferenceId, conferenceWindow)
      conferenceWindow.focus()
    }
  }

  public leaveConference(conference: ConferenceInfo) {
    this.conferenceWindowHandles.delete(conference.conferenceId)
    this.rSocketService.fireAndForget("socket/conference/leave", conference)
  }

  public inviteToConference(invitee: UserCredentials, inviter: UserCredentials, conferenceInfo: ConferenceInfo) {
    const invitationEvent = new InvitationEvent()
    invitationEvent.invitee = invitee
    invitationEvent.inviter = inviter
    if (conferenceInfo.conferenceId == null) {
      // Conference needs to be created
      this.createConference(conferenceInfo).subscribe(conferenceInfo => {
        invitationEvent.conferenceInfo = conferenceInfo
        this.sendInvitation(invitationEvent)
      })
    } else {
      invitationEvent.conferenceInfo = conferenceInfo
      // Conference already exists
      this.sendInvitation(invitationEvent)
    }
  }

  private sendInvitation(invitationEvent: InvitationEvent) {
    this.rSocketService.fireAndForget("socket/conference/invite", invitationEvent)
  }

  private initWindowHandle() {
    this.conferenceClosedSubject = new Subject<ConferenceOpenInfo>();
    this.conferenceClosedSubject.asObservable().pipe(
      tap(conference => {
          if (conference.isClosed) {
            const conferenceInfo = this.conferences.get(conference.conferenceId)
            if (conferenceInfo !== undefined) {
              this.leaveConference(conferenceInfo)
              this.conferenceWindowHandles.delete(conference.conferenceId)
              this.publish()
            } else {
              throw new Error("Conference " + conference.conferenceId + "not found in attendedConferences!")
            }
        }
      })
    ).subscribe()

    setInterval(() => {
      if (this.conferenceWindowHandles.size > 0) {
        this.conferenceWindowHandles.forEach((window, conferenceId) => {
            this.conferenceClosedSubject.next(new ConferenceOpenInfo(conferenceId, window.closed))
        })
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

