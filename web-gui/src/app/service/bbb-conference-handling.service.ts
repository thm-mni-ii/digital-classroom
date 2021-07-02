import {Injectable} from '@angular/core';
import {Observable, BehaviorSubject, Subscription} from 'rxjs';
import {Conference} from '../model/Conference';

/**
 * Handles the creation and retrievement of conference links.
 * @author Andrej Sajenko & Dominik Kröll
 */
@Injectable({
  providedIn: 'root'
})
export class BbbConferenceHandlingService {
  private personalConferenceLink: BehaviorSubject<string>;
  private bbbConferenceLink: BehaviorSubject<object>;
  private conference: BehaviorSubject<Conference>;
  public selectedConferenceSystem: BehaviorSubject<string>;
  public conferenceTimeoutTimer: Subscription;

  public constructor() {
     this.personalConferenceLink = new BehaviorSubject<string>(null);
     this.bbbConferenceLink = new  BehaviorSubject<object>(null);
     this.selectedConferenceSystem = new BehaviorSubject<string>('bigbluebutton');
     this.conference = new BehaviorSubject<Conference>(null);
  }

  public getSelectedConferenceSystem(): Observable<string> {
    return this.selectedConferenceSystem.asObservable();
  }

  public setSelectedConferenceSystem(service: string) {
    return this.selectedConferenceSystem.next(service);
  }

  public getConferenceConference(): Observable<Conference> {
    return this.conference.asObservable();
  }
}
