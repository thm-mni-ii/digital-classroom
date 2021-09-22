import {Component, Inject, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {TitlebarService} from '../../service/titlebar.service';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {DomSanitizer} from '@angular/platform-browser';
import {DOCUMENT} from '@angular/common';
import {Subscription} from 'rxjs';
import {ClassroomService} from '../../service/classroom.service';
import {InviteToConferenceDialogComponent} from '../../dialogs/invite-to-conference-dialog/invite-to-conference-dialog.component';
import {AssignTicketDialogComponent} from '../../dialogs/assign-ticket-dialog/assign-ticket-dialog.component';
import {Ticket} from '../../model/Ticket';
import {parseCourseRole, User, UserDisplay} from "../../model/User";
import {TicketService} from "../../service/ticket.service";
import {UserService} from "../../service/user.service";
import {ConferenceService} from "../../service/conference.service";
import {ConferenceInfo} from "../../model/ConferenceInfo";

@Component({
  selector: 'app-classroom',
  templateUrl: './classroom.component.html',
  styleUrls: ['./classroom.component.scss']
})
export class ClassroomComponent implements OnInit, OnDestroy {

  currentUser: User = undefined
  users: UserDisplay[] = [];
  tickets: Ticket[] = [];
  conferences: ConferenceInfo[] = [];
  attendedConferences: ConferenceInfo[] = [];
  subscriptions: Subscription[] = [];
  parseCourseRole: Function = parseCourseRole

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

  ngOnInit(): void {
    Notification.requestPermission().then();
    this.subscriptions.push(
      this.classroomService.currentUserObservable.subscribe(
      currentUser => this.currentUser = currentUser
      ),
      this.classroomService.tickets.subscribe(
      tickets => this.tickets = tickets
      ),
      this.classroomService.userObservable.subscribe(
      users => this.users = users
      ),
      this.classroomService.conferencesObservable.subscribe(
        conferences => this.conferences = conferences
      ),
      this.classroomService.attendedConferencesObservable.subscribe(
        attendedConferences => this.attendedConferences = attendedConferences
      )
    )
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe())
  }

  public inviteToConference(user: User) {
    this.dialog.open(InviteToConferenceDialogComponent, {
      height: 'auto',
      width: 'auto',
      data: user
    });
  }

  public assignTeacher(ticket) {
    if (this.classroomService.isCurrentUserAuthorized()) {
      this.dialog.open(AssignTicketDialogComponent, {
        height: 'auto',
        width: 'auto',
        data: ticket
      });
    }
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

}
