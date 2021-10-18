import {Component, Input} from '@angular/core';
import {Ticket} from "../../../model/Ticket";
import {ClassroomService} from "../../../service/classroom.service";
import {UserCredentials} from "../../../model/User";

@Component({
  selector: 'app-ticket-list',
  templateUrl: './ticket-list.component.html',
  styleUrls: ['./ticket-list.component.scss']
})
export class TicketListComponent {

  @Input() currentUser?: UserCredentials
  @Input() tickets: Ticket[] = []

  constructor(
    public classroomService: ClassroomService
  ) {
  }

  public sortTickets(tickets: Ticket[]) {
    if (tickets.length <= 0) return null
    return tickets.sort( (a, b) => {
      if (a.assignee?.userId === this.currentUser!!.userId && b.assignee?.userId === this.currentUser!!.userId) {
        return a.createTime > b.createTime ? 1 : -1;
      } else if (a.assignee?.userId === this.currentUser!!.userId) {
        return -1;
      } else if (b.assignee?.userId === this.currentUser!!.userId) {
        return 1;
      }
      return a.createTime > b.createTime ? 1 : -1;
    });
  }

}
