import {Component, Input} from '@angular/core';
import {Ticket} from "../../../../model/Ticket";
import {TimeFormatterService} from "../../../../util/time-formatter.service";
import {ClassroomService} from "../../../../service/classroom.service";

@Component({
  selector: 'app-ticket',
  templateUrl: './ticket.component.html',
  styleUrls: ['./ticket.component.scss']
})
export class TicketComponent {

  @Input() ticket: Ticket;
  @Input() i: number;

  constructor(
    private timeFormatterService: TimeFormatterService,
    private classroomService: ClassroomService
  ) {
  }
    getTicketTime(ticket: Ticket): string {
      return this.timeFormatterService.timeAgo(ticket.createTime)
    }

  inviteCreator() {

  }

  editTicket() {

  }

  closeTicket() {
    this.classroomService.closeTicket(this.ticket)
  }
}
