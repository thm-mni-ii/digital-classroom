import {Component, Input} from '@angular/core';
import {ConferenceInfo} from "../../../../model/ConferenceInfo";
import {User} from "../../../../model/User";
import {ConferenceService} from "../../../../service/conference.service";

@Component({
  selector: 'app-conference',
  templateUrl: './conference.component.html',
  styleUrls: ['./conference.component.scss']
})
export class ConferenceComponent {

  @Input() conference?: ConferenceInfo
  @Input() currentUser?: User

  constructor(private conferenceService: ConferenceService) { }

  public isUserAttending(): boolean {
    if (this.currentUser === undefined) return false;
    return this.conference!!.attendees.includes(this.currentUser.userId);
  }

  public changeVisibility() {
    this.conferenceService.changeVisibility(this.conference!!, !this.conference!!.visible);
  }

  public closeConference() {
    this.conferenceService.leaveConference(this.conference!!)
  }

  public joinConference() {
    this.conferenceService.joinConference(this.conference!!)
  }

  public joinTooltip(): string {
    if (this.isUserAttending()) return "Tab fokussieren"
    else return "Beitreten"
  }
}
