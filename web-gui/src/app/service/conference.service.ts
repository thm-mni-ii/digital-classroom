import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable, Subject} from 'rxjs';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Conference} from "../model/Conference";
import {distinctUntilChanged} from "rxjs/operators";

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
  private conferenceWindowHandle: Window;
  private isWindowhandleOpen: Subject<Boolean>;
  conferenceWindowOpen: Boolean = false;

  public constructor(private http: HttpClient) {
    this.isWindowhandleOpen = new Subject<Boolean>();
    this.isWindowhandleOpen.asObservable().pipe(distinctUntilChanged()).subscribe((isOpen) => {
      if (!isOpen) {
        this.closeConference();
      }
    });
    this.personalConferenceLink = new BehaviorSubject<string>("");
    this.bbbConferenceLink = new  BehaviorSubject<object>({});
    setInterval(() => {
      if (this.conferenceWindowHandle) {
        if (this.conferenceWindowHandle.closed) {
          this.isWindowhandleOpen.next(false);
        } else {
          this.isWindowhandleOpen.next(true);
        }
      }
    }, 1000);
  }

  public createConference() {
    if (this.conferenceWindowHandle == undefined || this.conferenceWindowHandle.closed) {
      this.http.get<Conference>("/classroom-api/conference/create").subscribe(conference => {
        this.joinConference(conference)
      })
    }
  }

  public joinConference(conference: Conference) {
    const options = { responseType: "text" as "json"};
    if (this.conferenceWindowHandle == undefined || this.conferenceWindowHandle.closed) {
      this.http.post<string>("/classroom-api/conference/join", conference, options).subscribe(url => {
        this.conferenceWindowHandle = open(url.toString())
        this.conferenceWindowOpen = true
      })
    } else {
      this.conferenceWindowHandle.focus()
    }
  }

  public closeConference() {
    if (this.conferenceWindowHandle && !this.conferenceWindowHandle.closed) {
      this.conferenceWindowHandle.close();
    }
    this.conferenceWindowOpen = false
  }

  public getConferenceWindowHandle(): Observable<Boolean> {
    return this.isWindowhandleOpen.asObservable()
  }
}
