import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ClassroomService} from '../../service/classroom.service';
import {AuthService} from '../../service/auth.service';
import {Ticket} from '../../model/Ticket';
import {UserCredentials, User} from "../../model/User";
import {TicketService} from "../../service/ticket.service";
import {ConferenceService} from "../../service/conference.service";

@Component({
  selector: 'app-assign-ticket-dialog',
  templateUrl: './assign-ticket-dialog.component.html',
  styleUrls: ['./assign-ticket-dialog.component.scss']
})
export class AssignTicketDialogComponent implements OnInit {
  users: User[] = [];
  disabled = false;

  constructor(@Inject(MAT_DIALOG_DATA) public data: Ticket,
              public dialogRef: MatDialogRef<AssignTicketDialogComponent>,
              private snackBar: MatSnackBar,
              public classroomService: ClassroomService,
              private conferenceService: ConferenceService,
              private ticketService: TicketService,
              public auth: AuthService) {
  }

  ngOnInit(): void {
    this.classroomService.userDisplayObservable.subscribe((users) => {
      this.users = users
    });
    this.dialogRef.afterOpened().subscribe(() => this.disabled = false);
  }

  public assignTicket(assignee, ticket) {
      this.data.assignee = assignee;
      this.ticketService.updateTicket(ticket);
      this.snackBar.open(`${assignee.fullName} wurde dem Ticket als Bearbeiter zugewiesen`, 'OK', {duration: 3000});
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
    this.classroomService.inviteToConference(invitee, null, this.data);
    this.snackBar.open(`${invitee.fullName} wurde eingeladen der Konferenz beizutreten.`, 'OK', {duration: 3000});
    this.dialogRef.close();
  }

  public joinConference(user: UserCredentials) {
    this.classroomService.joinConferenceOfUser(user);
  }
}
