import {Component, Input} from '@angular/core';
import {AssignTicketDialogComponent} from "../../../dialogs/assign-ticket-dialog/assign-ticket-dialog.component";
import {Ticket} from "../../../model/Ticket";
import {ClassroomService} from "../../../service/classroom.service";
import {MatDialog} from "@angular/material/dialog";
import {User} from "../../../model/User";

@Component({
  selector: 'app-ticket-list',
  templateUrl: './ticket-list.component.html',
  styleUrls: ['./ticket-list.component.scss']
})
export class TicketListComponent {

  @Input() currentUser: User
  @Input() tickets: Ticket[]

  constructor(
    public classroomService: ClassroomService,
    private dialog: MatDialog,
  ) { }

  public assignTeacher(ticket: Ticket) {
    this.dialog.open(AssignTicketDialogComponent, {
      height: 'auto',
      width: 'auto',
      data: ticket
    });
  }

  public sortTickets(tickets: Ticket[]) {
    if (tickets.length <= 0) return null
    return tickets.sort( (a, b) => {
      if (a.assignee?.userId === this.currentUser.userId && b.assignee?.userId === this.currentUser.userId) {
        return a.createTime > b.createTime ? 1 : -1;
      } else if (a.assignee?.userId === this.currentUser.userId) {
        return -1;
      } else if (b.assignee?.userId === this.currentUser.userId) {
        return 1;
      }
      return a.createTime > b.createTime ? 1 : -1;
    });
  }

}
