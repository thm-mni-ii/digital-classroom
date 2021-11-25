import {Component, OnInit} from '@angular/core';
import {ConferenceInfo} from "../../../model/ConferenceInfo";
import {User} from "../../../model/User";
import {ClassroomService} from "../../../service/classroom.service";

@Component({
  selector: 'app-conference-list',
  templateUrl: './conference-list.component.html',
  styleUrls: ['./conference-list.component.scss']
})
export class ConferenceListComponent implements OnInit {

  conferences: ConferenceInfo[] | undefined
  currentUser: User | undefined
  plenaryConference: ConferenceInfo | undefined

  constructor(
    public classroomService: ClassroomService,
  ) { }

  public ngOnInit(): void {
    this.classroomService.currentUserObservable.subscribe(
      currentUser => this.currentUser = currentUser
    )
    this.classroomService.conferencesObservable.subscribe(
      conferences => this.setConferences(conferences)
    )
    this.classroomService.classroomInfoObservable.subscribe(
      _ => this.setConferences(this.conferences!!)
    )
  }

  public isPlenaryConference(conference: ConferenceInfo): boolean {
    return conference.conferenceId === this.classroomService.classroomInfo!!.plenaryConference
  }

  private setConferences(conferences: ConferenceInfo[]) {
    this.plenaryConference = conferences.find(conf => this.isPlenaryConference(conf))
    if (this.plenaryConference !== undefined) {
      this.conferences = conferences.filter(conf => !this.isPlenaryConference(conf))
      this.conferences.unshift(this.plenaryConference)
    } else this.conferences = conferences
  }
}
