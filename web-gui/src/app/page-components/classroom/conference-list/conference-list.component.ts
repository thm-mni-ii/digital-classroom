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

  conferences: ConferenceInfo[]
  currentUser: User

  constructor(private classroomService: ClassroomService) { }

  isUserAttending(conference: ConferenceInfo): boolean {
    return conference.attendees.includes(this.currentUser.userId)
  }

  ngOnInit(): void {
    this.classroomService.currentUserObservable.subscribe(
      currentUser => this.currentUser = currentUser
    )
    this.classroomService.conferencesObservable.subscribe(
      conferences => this.conferences = conferences
    )
  }

}
