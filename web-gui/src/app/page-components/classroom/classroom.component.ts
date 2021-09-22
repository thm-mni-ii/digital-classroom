import {Component, Inject, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {TitlebarService} from '../../service/titlebar.service';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {DomSanitizer} from '@angular/platform-browser';
import {DOCUMENT} from '@angular/common';
import {Subscription} from 'rxjs';
import {ClassroomService} from '../../service/classroom.service';
import {Ticket} from '../../model/Ticket';
import {User, UserDisplay} from "../../model/User";
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

}
