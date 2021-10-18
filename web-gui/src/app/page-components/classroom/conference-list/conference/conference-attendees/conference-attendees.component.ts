import {Component, Input} from '@angular/core';
import {UserIconService} from "../../../../../util/user-icon.service";
import {ConferenceInfo} from "../../../../../model/ConferenceInfo";
import {UserService} from "../../../../../service/user.service";

@Component({
  selector: 'app-conference-attendees',
  templateUrl: './conference-attendees.component.html',
  styleUrls: ['./conference-attendees.component.scss']
})
export class ConferenceAttendeesComponent {

  @Input() conference?: ConferenceInfo

  constructor(
    public userIconService: UserIconService,
    private userService: UserService
  ) {
  }

  public getAttendees() {
    return this.conference!!.attendeeIds.map((userId) => this.userService.getFullUser(userId)!!).filter(user => user !== undefined)
  }

  public getClassOfNumAttendees(element: HTMLDivElement): string {
    if (this.isOverflown(element.firstElementChild) || this.isOverflown(element.children.item(1))) return "show-num-attendees";
    else return "hide-num-attendees";
  }

  public isOverflown(element: Element | null): boolean {
    if (element === null) return false
    return element.scrollHeight > element.clientHeight || element.scrollWidth > element.clientWidth;
  }
}
