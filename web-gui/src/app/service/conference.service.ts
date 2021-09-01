import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable, Subject} from 'rxjs';
import {ConferenceInfo} from "../model/Conference";
import {finalize, map} from "rxjs/operators";
import {RSocketService} from "../rsocket/r-socket.service";
import {EventListenerService} from "../rsocket/event-listener.service";
import {ConferenceEvent} from "../rsocket/event/ClassroomEvent";

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
  conferenceObservable: Observable<ConferenceInfo[]> = this.conferenceSubject.asObservable()

  private conferenceWindowHandle: Window;


  conferenceWindowOpen: boolean = false
  private conferenceInfo: ConferenceInfo;

  constructor(
    private rSocketService: RSocketService,
    private eventListenerService: EventListenerService
  ) {
    this.initConferences()
    this.eventListenerService.conferenceEvents.pipe(
      map((conferenceEvent: ConferenceEvent) => this.handleConferenceEvent(conferenceEvent)),
      map(() => this.publish())
    ).subscribe()
  }

  private initConferences() {
    this.rSocketService.requestStream<ConferenceInfo>("socket/init-conferences", "Init Conferences").pipe(
      map(conf => this.conferences.set(conf.conferenceId, conf)),
      finalize(() => this.publish())
    ).subscribe()
  }

  private handleConferenceEvent(conferenceEvent: ConferenceEvent) {
  }

  private publish() {
  }

  public createConference() {
    const conferenceEvent = new ConferenceEvent()
    this.rSocketService.requestResponse("socket/conference/create", conferenceEvent)
  }

  public joinConference(conference: ConferenceInfo) {
    this.rSocketService.requestResponse("socket/conference/join", conference.conferenceId)
    // USER_JOIN Event
    this.rSocketService.fireAndForget("socket/classroom-event")
  }

  public closeConference(conference: ConferenceInfo = this.conferenceInfo) {
    this.rSocketService.requestResponse("socket/conference/close", conference.conferenceId)
  }

}
