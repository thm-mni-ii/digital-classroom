import {Component, Input} from '@angular/core';
import {ConferenceInfo} from "../../../../model/ConferenceInfo";
import {User} from "../../../../model/User";

@Component({
  selector: 'app-conference',
  templateUrl: './conference.component.html',
  styleUrls: ['./conference.component.scss']
})
export class ConferenceComponent {

  @Input() conference?: ConferenceInfo
  @Input() currentUser?: User

  constructor() { }

  isUserAttending(conference: ConferenceInfo): boolean {
    if (this.currentUser === undefined) return false;
    return conference.attendees.includes(this.currentUser.userId);
  }

  closeConference() {

  }
}
