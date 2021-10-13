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
              public auth: AuthService) { }

  ngOnInit(): void {
    this.classroomService.userDisplayObservable.subscribe((users) => {
      this.users = users
    });
    this.dialogRef.afterOpened().subscribe(() => this.disabled = false);
  }

  public assignTicket(assignee?: UserCredentials, ticket?: Ticket) {
    if (assignee === undefined) throw new Error("assignee is undefined!")
    if (ticket === undefined) throw new Error("ticket is undefined!")
    this.data.assignee = assignee;
      this.ticketService.updateTicket(ticket);
      this.snackBar.open(`${assignee.fullName} wurde dem Ticket als Bearbeiter zugewiesen`, 'OK', {duration: 3000});
      this.dialogRef.close();
  }

  public startCall(invitee?: UserCredentials) {
    if (invitee === undefined) throw new Error("invitee is undefined!")
    if (this.disabled) {
      return;
    }
    this.disabled = true;
    this.classroomService.inviteToConference(invitee, undefined, this.data);
    this.snackBar.open(`${invitee.fullName} wurde eingeladen der Konferenz beizutreten.`, 'OK', {duration: 3000});
    this.dialogRef.close();
  }

  public joinConference(user?: UserCredentials) {
    if (user === undefined) throw new Error("user to join is undefined!")
    this.classroomService.chooseConferenceToJoin(user);
  }
}
