import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import { first } from 'rxjs/operators';
import {ClassroomService} from '../../service/classroom.service';
import {AuthService} from '../../service/auth.service';
import {Ticket} from '../../model/Ticket';
import {User} from "../../model/User";
import {TicketService} from "../../service/ticket.service";
import {UserService} from "../../service/user.service";
import {ConferenceService} from "../../service/conference.service";

@Component({
  selector: 'app-manage-ticket-dialog',
  templateUrl: './manage-ticket-dialog.component.html',
  styleUrls: ['./manage-ticket-dialog.component.scss']
})
export class ManageTicketDialogComponent implements OnInit {
  ticket: Ticket;
  users: User[] = [];
  disabled = false;

  constructor(@Inject(MAT_DIALOG_DATA) public data: any,
              public dialogRef: MatDialogRef<ManageTicketDialogComponent>,
              private snackBar: MatSnackBar,
              public classroomService: ClassroomService,
              private conferenceService: ConferenceService,
              private ticketService: TicketService,
              public auth: AuthService) {
    this.ticket = this.data.ticket;
  }

  ngOnInit(): void {
    this.classroomService.userObservable.subscribe((users) => {
      this.users = users
    });
    this.dialogRef.afterOpened().subscribe(() => this.disabled = false);
  }

  public assignTicket(assignee, ticket) {
      this.ticket.assignee = assignee;
      this.ticketService.updateTicket(ticket);
      this.snackBar.open(`${assignee.prename} ${assignee.surname} wurde dem Ticket als Bearbeiter zugewiesen`, 'OK', {duration: 3000});
      this.dialogRef.close();
  }

  public closeTicket(ticket) {
    this.ticketService.removeTicket(ticket);
    this.snackBar.open(`Das Ticket wurde geschlossen`, 'OK', {duration: 3000});
    this.dialogRef.close();
  }

  public startCall(invitee) {
    if (this.disabled) {
      return;
    }
    this.disabled = true;
    this.classroomService.userInviter().pipe(first()).subscribe(() => {
      this.classroomService.inviteToConference(invitee);
    });
    this.classroomService.openConference();
    this.snackBar.open(`${invitee.prename} ${invitee.surname} wurde eingeladen der Konferenz beizutreten.`, 'OK', {duration: 3000});
    this.dialogRef.close();
  }
  public isInConference(user: User): boolean {
    return false
    // return this.usersInConference.filter(u => u.userId === user.userId).length !== 0;
  }
  public joinConference(user: User) {
    this.classroomService.joinConference(user);
  }
}
