import {Component, Inject, OnDestroy, OnInit} from '@angular/core';
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
import {User, UserDisplay} from "../../model/User";
import {TicketService} from "../../service/ticket.service";
import {UserService} from "../../service/user.service";
import {ConferenceService} from "../../service/conference.service";
import {ConferenceInfo} from "../../model/Conference";
import {ClassroomInfo} from "../../model/ClassroomInfo";

@Component({
  selector: 'app-conference',
  templateUrl: './classroom.component.html',
  styleUrls: ['./classroom.component.scss']
})
export class ClassroomComponent implements OnInit, OnDestroy {
  constructor(private route: ActivatedRoute,
              private titlebarService: TitlebarService,
              public conferenceService: ConferenceService,
              public classroomService: ClassroomService,
              private dialog: MatDialog,
              private snackbar: MatSnackBar,
              private sanitizer: DomSanitizer,
              private router: Router,
              private ticketService: TicketService,
              private userService: UserService,
              @Inject(DOCUMENT) document) {
  }
  classroomInfo: ClassroomInfo = undefined
  currentUser: UserDisplay = undefined
  users: User[] = [];
  tickets: Ticket[] = [];
  conferences: ConferenceInfo[] = [];
  usersInConference: User[] = [];
  isCourseSubscriber: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  subscriptions: Subscription[] = [];
  intervalID;

  ngOnInit(): void {
    Notification.requestPermission().then();
    this.classroomService.currentUserObservable.subscribe(
      currentUser => this.currentUser = currentUser
    )
    this.classroomService.classroomInfo.subscribe(
      classroomInfo => this.classroomInfo = classroomInfo
    )
    this.classroomService.tickets.subscribe(
      tickets => this.tickets = tickets
    )
    this.classroomService.users.subscribe(
      users => this.users = users
    )
  }

  ngOnDestroy(): void {
    clearInterval(this.intervalID);
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
      data: {courseID: this.classroomInfo.classroomId, ticket: ticket}
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
        this.classroomService.createTicket(ticket);
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

}
