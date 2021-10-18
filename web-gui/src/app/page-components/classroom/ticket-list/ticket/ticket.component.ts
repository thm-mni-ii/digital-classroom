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
    private classroomService: ClassroomService
  ) { }

  ngOnInit(): void {
    if (this.ticket === undefined) throw new Error("Ticket is undefined!")
    if (this.ticket?.creator === undefined) throw new Error("Ticket " + this.ticket?.ticketId + " without creator")
  }

  getTicketTime(ticket: Ticket): string {
    return this.timeFormatterService.timeAgo(ticket.createTime)
  }

  inviteCreator() {

  }

  editTicket() {

  }

  closeTicket() {
    this.classroomService.closeTicket(this.ticket!!)
  }
}
