import {Component, Input} from '@angular/core';
import {ConferenceInfo} from "../../../model/ConferenceInfo";
import {UserDisplay} from "../../../model/User";

@Component({
  selector: 'app-conferences',
  templateUrl: './conferences.component.html',
  styleUrls: ['./conferences.component.scss']
})
export class ConferencesComponent {

  @Input() conferences: ConferenceInfo[]
  @Input() currentUser: UserDisplay

  constructor() { }

  isUserAttending(conference: ConferenceInfo): boolean {
    return conference.attendees.includes(this.currentUser.userId)
  }



}
