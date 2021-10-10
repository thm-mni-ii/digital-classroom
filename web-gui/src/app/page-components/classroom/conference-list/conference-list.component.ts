import {Component, Input} from '@angular/core';
import {ConferenceInfo} from "../../../model/ConferenceInfo";
import {User} from "../../../model/User";

@Component({
  selector: 'app-conference-list',
  templateUrl: './conference-list.component.html',
  styleUrls: ['./conference-list.component.scss']
})
export class ConferenceListComponent {

  @Input() conferences: ConferenceInfo[]
  @Input() currentUser: User

  constructor() { }

  isUserAttending(conference: ConferenceInfo): boolean {
    return conference.attendees.includes(this.currentUser.userId)
  }



}
