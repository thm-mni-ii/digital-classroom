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

  constructor(private classroomService: ClassroomService) { }

  isUserAttending(conference: ConferenceInfo): boolean {
    if (this.currentUser === undefined) return false;
    return conference.attendees.includes(this.currentUser.userId);
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
