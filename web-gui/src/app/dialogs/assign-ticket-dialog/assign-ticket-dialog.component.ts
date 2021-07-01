import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import { first } from 'rxjs/operators';
import {UserService} from '../../service/user.service';
import {ClassroomService} from '../../service/classroom.service';
import {ConferenceService} from '../../service/conference.service';
import {AuthService} from '../../service/auth.service';
import {Roles} from '../../model/Roles';
import {User} from '../../model/User';
import {Ticket} from '../../model/Ticket';

@Component({
  selector: 'app-assign-ticket-dialog',
  templateUrl: './assign-ticket-dialog.component.html',
  styleUrls: ['./assign-ticket-dialog.component.scss'],
  providers: [UserService]
})
export class AssignTicketDialogComponent implements OnInit {
  ticket: Ticket;
  courseID: number;
  users: User[] = [];
  usersInConference: User[] = [];
  disabled = false;

  constructor(@Inject(MAT_DIALOG_DATA) public data: any, public dialogRef: MatDialogRef<AssignTicketDialogComponent>,
              private snackBar: MatSnackBar, public classroomService: ClassroomService,
              private conferenceService: ConferenceService, public auth: AuthService, private dialog: MatDialog) {
    this.ticket = this.data.ticket;
    this.courseID = this.data.courseID;
  }

  ngOnInit(): void {
    this.classroomService.getUsersInConference().subscribe((users) => {
      this.usersInConference = users;
    });
    this.classroomService.getUsers().subscribe((users) => {
      this.users = users;
    });
    this.dialogRef.afterOpened().subscribe(() => this.disabled = false);
  }
  public assignTicket(assignee: User, ticket: Ticket) {
      this.ticket.assignee = assignee;
      this.classroomService.updateTicket(ticket);
      this.snackBar.open(`${assignee.prename} ${assignee.surname} wurde dem Ticket als Bearbeiter zugewiesen`, 'OK', {duration: 3000});
      this.dialogRef.close();
    }

  public closeTicket(ticket: Ticket) {
    this.classroomService.removeTicket(ticket);
    this.snackBar.open(`Das Ticket wurde geschlossen`, 'OK', {duration: 3000});
    this.dialogRef.close();
  }

  public isAuthorized() {
    const courseRole = this.auth.getToken().courseRoles[this.courseID];
    return Roles.CourseRole.isDocent(courseRole) || Roles.CourseRole.isTutor(courseRole);
  }

  public startCall(invitee: User[]) {
    if (this.disabled) {
      return;
    }
    this.disabled = true;
    this.classroomService.userInviter().pipe(first()).subscribe(() => {
      this.classroomService.inviteToConference(invitee);
    });
    this.classroomService.openConference();
    this.snackBar.open(`${invitee[0].prename} ${invitee[0].surname} wurde eingeladen der Konferenz beizutreten.`, 'OK', {duration: 3000});
    this.dialogRef.close();
  }
  public isInConference(user: User) {
    return this.usersInConference.filter(u => u.username === user.username).length !== 0;
  }
  public joinConference(user: User) {
    this.classroomService.joinConference(user);
  }
}
