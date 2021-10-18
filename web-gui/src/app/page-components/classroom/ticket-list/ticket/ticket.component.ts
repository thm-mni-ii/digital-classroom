import {Component, Input, OnInit} from '@angular/core';
import {Ticket} from "../../../../model/Ticket";
import {TimeFormatterService} from "../../../../util/time-formatter.service";
import {ClassroomService} from "../../../../service/classroom.service";
import {ConferenceInfo} from "../../../../model/ConferenceInfo";
import {User, UserCredentials} from "../../../../model/User";
import {UserService} from "../../../../service/user.service";

@Component({
  selector: 'app-ticket',
  templateUrl: './ticket.component.html',
  styleUrls: ['./ticket.component.scss']
})
export class TicketComponent implements OnInit {

  @Input() ticket?: Ticket;
  @Input() users: User[] = []
  conference?: ConferenceInfo;

  constructor(
    private timeFormatterService: TimeFormatterService,
    public classroomService: ClassroomService,
    public userService: UserService
  ) { }

  ngOnInit(): void {
    if (this.ticket === undefined) throw new Error("Ticket is undefined!")
    if (this.ticket?.creator === undefined) throw new Error("Ticket " + this.ticket?.ticketId + " without creator")
  }

  getTicketTime(ticket: Ticket): string {
    return this.timeFormatterService.timeAgo(ticket.createTime)
  }

  public determineButton(): "join" | "link" | "invite" {
    this.conference = this.classroomService.findConferenceOfTicket(this.ticket!!)
    if (this.classroomService.isSelf(this.ticket!!.creator) && this.conference === undefined) {
      return "link"
    }
    if (this.conference !== undefined || this.classroomService.isInConference(this.ticket!!.creator))
      return "join"
    else
      return "invite"
  }

  editTicket() {
    this.classroomService.createOrEditTicket(this.ticket)
  }

  closeTicket() {
    this.classroomService.closeTicket(this.ticket!!)
  }

  inviteCreator() {
    this.classroomService
      .createNewConferenceForTicket(this.ticket!!)
      .subscribe(conf => this.classroomService.inviteToConference(this.ticket!!.creator, conf))
  }

  linkConference() {
    this.classroomService.linkTicketToConference(this.ticket!!)
  }

  joinConference() {
    this.classroomService.conferenceService.joinConference(this.conference!!)
  }

  public fullUser(user: UserCredentials): User | undefined {
    return this.userService.getFullUser(user.userId)
  }
}
