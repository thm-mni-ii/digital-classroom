import {Component, Inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {TitlebarService} from '../../service/titlebar.service';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {DomSanitizer} from '@angular/platform-browser';
import {DOCUMENT} from '@angular/common';
import {Subscription, BehaviorSubject} from 'rxjs';
import {ClassroomService} from '../../service/classroom.service';
import {NewTicketDialogComponent} from '../../dialogs/newticket-dialog/new-ticket-dialog.component';
import {AuthService} from '../../service/auth.service';
import {Roles} from '../../model/Roles';
import {InviteToConferenceDialogComponent} from '../../dialogs/inviteto-conference-dialog/invite-to-conference-dialog.component';
import {AssignTicketDialogComponent} from '../../dialogs/assign-ticket-dialog/assign-ticket-dialog.component';
import {Ticket} from '../../model/Ticket';
import {User} from "../../model/User";
import {TicketService} from "../../service/ticket.service";
import {UserService} from "../../service/user.service";
import {ConferenceService} from "../../service/conference.service";

@Component({
  selector: 'app-conference',
  templateUrl: './classroom.component.html',
  styleUrls: ['./classroom.component.scss']
})
export class ClassroomComponent implements OnInit {
  constructor(private route: ActivatedRoute,
              private titlebarService: TitlebarService,
              public conferenceService: ConferenceService,
              public classroomService: ClassroomService,
              private dialog: MatDialog,
              public auth: AuthService,
              private snackbar: MatSnackBar,
              private sanitizer: DomSanitizer,
              private router: Router,
              private ticketService: TicketService,
              private userService: UserService,
              @Inject(DOCUMENT) document) {
  }
  classroomId: string;
  users: User[] = [];
  usersInConference: User[] = [];
  tmpUsersInConference: User[] = [];
  tickets: Ticket[] = []
  self: User;
  isCourseSubscriber: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  subscriptions: Subscription[] = [];
  intervalID;

  ngOnInit(): void {
    const token = this.auth.getToken()
    this.self = token
    this.classroomId = token.classroomId
    this.joinClassroom(token);

    this.route.params.subscribe(param => {
        this.classroomId = param.id;
      });
    this.classroomService.getUsersInConference().subscribe((users) => {
      this.tmpUsersInConference = users;
    });
    this.userService.getUsersInClassroom().then(users =>
      this.users = users
    )
    setTimeout(() => this.refresh(), 1000);
    this.intervalID = setInterval(() => this.refresh(), 10000);
  }

  ngOnDestroy(): void {
    clearInterval(this.intervalID);
  }

  public isAuthorized() {
    const courseRole = this.auth.getToken().userRole
    return Roles.isPrivileged(courseRole)
  }

  public inviteToConference(users) {
    this.dialog.open(InviteToConferenceDialogComponent, {
      height: 'auto',
      width: 'auto',
      data: {users: users}
    });
  }

  public assignTeacher(ticket) {
    this.dialog.open(AssignTicketDialogComponent, {
      height: 'auto',
      width: 'auto',
      data: {courseID: this.classroomId, ticket: ticket}
    });
  }

  public sortTickets(tickets: Ticket[]) {
    if (tickets.length <= 0) return null
    return tickets.sort( (a, b) => {
      const userId: String = this.auth.getToken().userId;
      if (a.assignee?.userId === userId && b.assignee?.userId === userId) {
        return a.createTime > b.createTime ? 1 : -1;
      } else if (a.assignee?.userId === userId) {
        return -1;
      } else if (b.assignee?.userId === userId) {
        return 1;
      }
      return a.createTime > b.createTime ? 1 : -1;
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

  joinClassroom(user: User) {
    Notification.requestPermission().then();
    this.classroomService.join(user);
  }

  leaveClassroom() {
    this.classroomService.leave();
  }

  public parseCourseRole(role: String): String {
    switch (role) {
      case 'TEACHER': return 'Dozent';
      case 'TUTOR': return 'Tutor';
      case 'STUDENT': return 'Student';
      default: return "Student";
    }
  }
  public createTicket() {
    this.dialog.open(NewTicketDialogComponent, {
      height: 'auto',
      width: 'auto',
    }).beforeClosed().subscribe((ticket: Ticket) => {
      if (ticket) {
        ticket.creator = this.self
        this.ticketService.createTicket(ticket);
      }
    });
  }

  public isInConference(user: User) {
    return this.usersInConference.filter(u => u.userId === user.userId).length !== 0;
  }

  public isInConferenceId(userId: string) {
    return this.usersInConference.filter(u => u.userId === userId).length !== 0;
  }
  public isInClassroom(userId: string) {
    return this.users.filter(u => u.userId === userId).length !== 0;
  }

  private refresh() {
    this.ticketService.getTickets().then(tickets => {
      this.tickets = tickets
    })

    this.userService.getUsersInClassroom().then(users => {
      this.users = users
    })
    //this.users = this.sortUsers(JSON.parse(JSON.stringify(this.users)));
    //this.usersInConference = this.sortUsers(JSON.parse(JSON.stringify(this.tmpUsersInConference)));
  }
}
