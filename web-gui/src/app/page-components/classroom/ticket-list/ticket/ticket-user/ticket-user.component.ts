import {Component, Input} from '@angular/core';
import {UserIconService} from "../../../../../util/user-icon.service";
import {User, UserCredentials} from "../../../../../model/User";
import {Ticket} from "../../../../../model/Ticket";
import {MatDialog} from "@angular/material/dialog";
import {AssignTicketDialogComponent} from "../../../../../dialogs/assign-ticket-dialog/assign-ticket-dialog.component";
import {UserService} from "../../../../../service/user.service";

@Component({
  selector: 'app-ticket-user',
  templateUrl: './ticket-user.component.html',
  styleUrls: ['./ticket-user.component.scss']
})
export class TicketUserComponent {

  @Input() userCredentials?: UserCredentials;
  @Input() ticket?: Ticket;
  @Input() ticketContext?: string;

  constructor(
    public userIconService: UserIconService,
    private dialog: MatDialog,
    private userService: UserService
  ) {
  }

  public assignUserTo() {
    this.dialog.open(AssignTicketDialogComponent, {
      height: 'auto',
      width: 'auto',
      data: this.ticket
    });
  }

  public fullUser(): User | undefined {
    if (this.userCredentials === undefined) throw new Error("Cannot get full user of undefined!")
    return this.userService.getFullUser(this.userCredentials.userId)
  }

}
