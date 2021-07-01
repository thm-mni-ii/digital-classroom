import {Injectable} from '@angular/core';
import {Observable, BehaviorSubject} from 'rxjs';
import {ConferenceSystems} from '../util/ConferenceSystems';
import {Conference} from '../model/Conference';

/**
 * Handles the creation and retrivement of conference links.
 * @author Andrej Sajenko
 */
@Injectable({
  providedIn: 'root'
})
export class ConferenceService {
  private personalConferenceLink: BehaviorSubject<string>;
  private bbbConferenceLink: BehaviorSubject<object>;
  private conference: BehaviorSubject<Conference> | undefined;
  public selectedConferenceSystem: BehaviorSubject<string>;

  public constructor() {
     this.personalConferenceLink = new BehaviorSubject<string>("");
     this.bbbConferenceLink = new  BehaviorSubject<object>({});
     this.selectedConferenceSystem = new BehaviorSubject<string>(ConferenceSystems.BigBlueButton);
     //this.conference = new BehaviorSubject<Conference>();
  }

  public getSelectedConferenceSystem(): Observable<string> {
    return this.selectedConferenceSystem.asObservable();
  }

  public setSelectedConferenceSystem(service: string) {
    return this.selectedConferenceSystem.next(service);
  }

  public getConferenceConference(): Observable<Conference> {
    return this.conference!.asObservable();
  }
}
