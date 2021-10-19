import {Component, Input} from '@angular/core';
import {ConferenceInfo} from "../../../../model/ConferenceInfo";
import {User} from "../../../../model/User";
import {ConferenceService} from "../../../../service/conference.service";
import {ClassroomService} from "../../../../service/classroom.service";
import {Ticket} from "../../../../model/Ticket";

@Component({
  selector: 'app-conference',
  templateUrl: './conference.component.html',
  styleUrls: ['./conference.component.scss']
})
export class ConferenceComponent {

  @Input() conference?: ConferenceInfo
  @Input() currentUser?: User

  ticket: Ticket | null = null

  constructor(
    public conferenceService: ConferenceService,
    public classroomService: ClassroomService
  ) {

  }

  public isUserAttending(): boolean {
    if (this.currentUser === undefined) return false;
    return this.conference!!.attendeeIds.includes(this.currentUser.userId);
  }

  public changeVisibility() {
    this.conferenceService.changeVisibility(this.conference!!, !this.conference!!.visible);
  }

  public joinConference() {
    this.conferenceService.joinConference(this.conference!!)
  }

  public leaveConference() {
    this.conferenceService.leaveConference(this.conference!!)
  }

  public endConference() {
    this.conferenceService.endConference(this.conference!!)
  }

  public joinTooltip(): string {
    if (this.isUserAttending()) return "Tab fokussieren"
    else return "Beitreten"
  }

  public hasTicketRef(): boolean {
    this.ticket = this.classroomService.findTicketOfConference(this.conference!!)
    return this.ticket !== null
  }
}
