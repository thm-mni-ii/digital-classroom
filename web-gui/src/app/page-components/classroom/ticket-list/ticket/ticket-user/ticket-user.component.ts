import {Component, Input} from '@angular/core';
import {UserIconService} from "../../../../../util/user-icon.service";
import {User} from "../../../../../model/User";
import {Ticket} from "../../../../../model/Ticket";
import {MatDialog} from "@angular/material/dialog";
import {AssignTicketDialogComponent} from "../../../../../dialogs/assign-ticket-dialog/assign-ticket-dialog.component";

@Component({
  selector: 'app-ticket-user',
  templateUrl: './ticket-user.component.html',
  styleUrls: ['./ticket-user.component.scss']
})
export class TicketUserComponent {

  @Input() user: User;
  @Input() ticket: Ticket;
  @Input() ticketContext: string;
  constructor(
    public userIconService: UserIconService,
    private dialog: MatDialog,
  ) {
  }

  public assignUserTo() {
    this.dialog.open(AssignTicketDialogComponent, {
      height: 'auto',
      width: 'auto',
      data: this.ticket
    });
  }

}
