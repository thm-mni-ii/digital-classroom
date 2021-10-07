import {Component, Input} from '@angular/core';
import {Ticket} from "../../../../model/Ticket";
import {TimeFormatterService} from "../../../../util/time-formatter.service";

@Component({
  selector: 'app-ticket',
  templateUrl: './ticket.component.html',
  styleUrls: ['./ticket.component.scss']
})
export class TicketComponent {

  @Input() ticket: Ticket;

  @Input() i: number;

  constructor(private timeFormatterService: TimeFormatterService) {
  }


    getTicketTime(ticket: Ticket): string {
      return this.timeFormatterService.timeAgo(ticket.createTime)
    }
}
