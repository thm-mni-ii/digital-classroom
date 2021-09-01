import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import { first } from 'rxjs/operators';
import {ClassroomService} from '../../service/classroom.service';
import {AuthService} from '../../service/auth.service';
import {Ticket} from '../../model/Ticket';
import {User, UserDisplay} from "../../model/User";
import {TicketService} from "../../service/ticket.service";
import {UserService} from "../../service/user.service";
import {ConferenceService} from "../../service/conference.service";

@Component({
  selector: 'app-assign-ticket-dialog',
  templateUrl: './assign-ticket-dialog.component.html',
  styleUrls: ['./assign-ticket-dialog.component.scss']
})
export class AssignTicketDialogComponent implements OnInit {
  ticket: Ticket;
  users: UserDisplay[] = [];
  disabled = false;

  constructor(@Inject(MAT_DIALOG_DATA) public data: Ticket,
              public dialogRef: MatDialogRef<AssignTicketDialogComponent>,
              private snackBar: MatSnackBar,
              public classroomService: ClassroomService,
              private conferenceService: ConferenceService,
              private ticketService: TicketService,
              public auth: AuthService) {
    this.ticket = data
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
  public isInConference(user: User) {
    return this.users.find(user => user.userId === user.userId).inConference
  }

  public joinConference(user: User) {
    this.classroomService.joinConferenceOfUser(user);
  }
}
