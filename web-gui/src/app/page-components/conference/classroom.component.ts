import {Component, Inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {TitlebarService} from '../../service/titlebar.service';
import {BbbConferenceHandlingService} from '../../service/bbb-conference-handling.service';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {DomSanitizer} from '@angular/platform-browser';
import {DOCUMENT} from '@angular/common';
import {Observable, Subscription, BehaviorSubject} from 'rxjs';
import {ClassroomService} from '../../service/classroom.service';
import {NewTicketDialogComponent} from '../../dialogs/newticket-dialog/new-ticket-dialog.component';
import {NewConferenceDialogComponent} from '../../dialogs/newconference-dialog/new-conference-dialog.component';
import {AuthService} from '../../service/auth.service';
import {Roles} from '../../model/Roles';
import {InviteToConferenceDialogComponent} from '../../dialogs/inviteto-conference-dialog/invite-to-conference-dialog.component';
import {AssignTicketDialogComponent} from '../../dialogs/assign-ticket-dialog/assign-ticket-dialog.component';
import {Ticket} from '../../model/Ticket';

@Component({
  selector: 'app-conference',
  templateUrl: './classroom.component.html',
  styleUrls: ['./classroom.component.scss']
})
export class ClassroomComponent implements OnInit {
  constructor(private route: ActivatedRoute,
              private titlebarService: TitlebarService,
              private conferenceService: BbbConferenceHandlingService,
              public classroomService: ClassroomService,
              private dialog: MatDialog,
              public auth: AuthService,
              private snackbar: MatSnackBar,
              private sanitizer: DomSanitizer,
              private router: Router,
              @Inject(DOCUMENT) document) {
  }
  courseId: number;
  users: User[] = [];
  tmpUsers: User[] = [];
  usersInConference: User[] = [];
  tmpUsersInConference: User[] = [];
  tickets: Observable<Ticket[]>;
  self: User;
  isCourseSubscriber: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  subscriptions: Subscription[] = [];
  username: string;
  conferenceWindowOpen: Boolean = false;
  intervalID;

  ngOnInit(): void {
    this.username = this.auth.getToken().id;
    this.tickets = this.classroomService.getTickets();
    this.route.params.subscribe(param => {
        this.courseId = param.id;
      });
    this.classroomService.getUsersInConference().subscribe((users) => {
      this.tmpUsersInConference = users;
    });
    this.classroomService.getUsers().subscribe((users) => {
      this.tmpUsers = users;
    });
    if (!this.classroomService.isJoined()) {
      this.joinClassroom();
    }
    this.classroomService.getConferenceWindowHandle().subscribe(isOpen => {
      this.conferenceWindowOpen = isOpen;
    });
    setTimeout(() => this.refresh(), 1000);
    this.intervalID = setInterval(() => this.refresh(), 10000);
  }

  ngOnDestroy(): void {
    clearInterval(this.intervalID);
  }

  public isAuthorized() {
    const courseRole = this.auth.getToken().role
    return Roles.isDocent(courseRole) || Roles.isTutor(courseRole);
  }

  public inviteToConference(users) {
    this.dialog.open(InviteToConferenceDialogComponent, {
      height: 'auto',
      width: 'auto',
      data: {users: users}
    });
  }

  public createConference() {
    this.dialog.open(NewConferenceDialogComponent, {
      height: 'auto',
      width: 'auto',
      data: {courseID: this.courseId}
    });
  }

  public assignTeacher(ticket) {
    this.dialog.open(AssignTicketDialogComponent, {
      height: 'auto',
      width: 'auto',
      data: {courseID: this.courseId, ticket: ticket}
    });
  }

  public sortTickets(tickets) {
    return tickets.sort( (a, b) => {
      const userId: String = this.auth.getToken().id;
      if (a.assignee.id === userId && b.assignee.id === userId) {
        return a.timestamp > b.timestamp ? 1 : -1;
      } else if (a.assignee.id === userId) {
        return -1;
      } else if (b.assignee.id === userId) {
        return 1;
      }
      return a.timestamp > b.timestamp ? 1 : -1;
    });
  }
  public sortUsers(users) {
    return users.sort((a, b) => {
      if (a.courseRole > b.courseRole) {
        return 1;
      } else if ( a.courseRole < b.courseRole) {
        return -1;
      } else {
        if (a.courseRole > b.courseRole) {
          return 1;
        } else {
          return -1;
        }
      }
    });
  }

  joinClassroom() {
    Notification.requestPermission();
    this.classroomService.join(this.courseId);
  }
  leaveClassroom() {
    this.classroomService.leave();
    this.router.navigate(['courses', this.courseId]);
  }
  public parseCourseRole(role: String): String {
    switch (role) {
      case 'DOCENT': return 'Dozent';
      case 'TUTOR': return 'Tutor';
      case 'STUDENT': return 'Student';
      default: return "Student";
    }
  }
  public createTicket() {
    this.dialog.open(NewTicketDialogComponent, {
      height: 'auto',
      width: 'auto',
      data: {courseID: this.courseId}
    }).afterClosed().subscribe(ticket => {
      if (ticket) {
        this.classroomService.createTicket(ticket);
      }
    });
  }
  public isInConference(user: User) {
    return this.usersInConference.filter(u => u.id === user.id).length !== 0;
  }
  public isInConferenceId(id: string) {
    return this.usersInConference.filter(u => u.id === id).length !== 0;
  }
  public isInClassroom(id: string) {
    return this.users.filter(u => u.id === id).length !== 0;
  }

  private refresh() {
    this.users = this.sortUsers(JSON.parse(JSON.stringify(this.tmpUsers)));
    this.usersInConference = this.sortUsers(JSON.parse(JSON.stringify(this.tmpUsersInConference)));
  }
}
