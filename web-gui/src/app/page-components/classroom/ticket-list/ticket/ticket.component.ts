import {Component, Input, OnInit} from '@angular/core';
import {Ticket} from "../../../../model/Ticket";
import {TimeFormatterService} from "../../../../util/time-formatter.service";
import {ClassroomService} from "../../../../service/classroom.service";

@Component({
  selector: 'app-ticket',
  templateUrl: './ticket.component.html',
  styleUrls: ['./ticket.component.scss']
})
export class TicketComponent implements OnInit {

  @Input() ticket?: Ticket;

  constructor(
    private timeFormatterService: TimeFormatterService,
    public classroomService: ClassroomService
  ) { }

  ngOnInit(): void {
    if (this.ticket === undefined) throw new Error("Ticket is undefined!")
    if (this.ticket?.creator === undefined) throw new Error("Ticket " + this.ticket?.ticketId + " without creator")
  }

  getTicketTime(ticket: Ticket): string {
    return this.timeFormatterService.timeAgo(ticket.createTime)
  }

  public determineJoinButton(): "join" | "link" | "invite" {
    if (this.ticket!!.conferenceId !== null && this.classroomService.isInConference(this.ticket!!.creator)) return "join"
    else if (this.classroomService.isSelf(this.ticket!!.creator)) return "link"
    else return "invite"
  }

  editTicket() {

  }

  closeTicket() {
    this.classroomService.closeTicket(this.ticket!!)
  }

  inviteCreator() {
    const conference = this.classroomService.findOrCreateConferenceOfTicket(this.ticket!!)
    this.classroomService.inviteToConference(this.ticket!!.creator, conference)
  }

  linkConference() {

  }

  joinConference() {

  }
}
