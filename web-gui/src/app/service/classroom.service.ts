import {Injectable} from '@angular/core';
import {Observable, Subject, BehaviorSubject} from 'rxjs';
import {AuthService} from './auth.service';
import {MatDialog} from '@angular/material/dialog';
import {IncomingCallDialogComponent} from '../dialogs/incoming-call-dialog/incoming-call-dialog.component';
import {UserCredentials, User} from "../model/User";
import {ConferenceService} from "./conference.service";
import {RSocketService} from "../rsocket/r-socket.service";
import {Ticket} from "../model/Ticket";
import {TicketService} from "./ticket.service";
import {
  filter,
  map,
  tap
} from "rxjs/operators";
import {UserService} from "./user.service";
import {Roles} from "../model/Roles";
import {NewTicketDialogComponent} from "../dialogs/new-ticket-dialog/new-ticket-dialog.component";
import {ConferenceInfo} from "../model/ConferenceInfo";
import {ClassroomInfo} from "../model/ClassroomInfo";
import {InvitationEvent} from "../rsocket/event/ClassroomEvent";
import {InviteToConferenceDialogComponent} from "../dialogs/invite-to-conference-dialog/invite-to-conference-dialog.component";
import {Router} from "@angular/router";
import {NotificationService} from "./notification.service";
import {JoinUserConferenceDialogComponent} from "../dialogs/join-user-conference-dialog/join-user-conference-dialog.component";
import {LogoutService} from "./logout.service";

/**
 * Service that provides observables that asynchronously updates tickets, users and
 * provide Conferences to take part in a conference.
 */
@Injectable({
  providedIn: 'root'
})
export class ClassroomService {

  public tickets = this.ticketService.ticketObservable
  public userDisplayObservable = this.userService.userObservable
  public currentUserObservable = this.userService.currentUserObservable
  public conferencesObservable = this.conferenceService.conferencesObservable.pipe(
    map(confs => confs.filter(conf => conf.visible || conf.creator!!.userId == this.currentUser?.userId))
  )
  private conferences: ConferenceInfo[] = []

  private users: User[] = []

  private classroomInfoSubject: Subject<ClassroomInfo> = new BehaviorSubject(new ClassroomInfo())
  classroomInfoObservable: Observable<ClassroomInfo> = this.classroomInfoSubject.asObservable()

  public classroomInfo: ClassroomInfo | undefined
  public currentUser: User | undefined

  public constructor(private router: Router,
                     private authService: AuthService,
                     private conferenceService: ConferenceService,
                     private dialog: MatDialog,
                     private rSocketService: RSocketService,
                     private ticketService: TicketService,
                     private userService: UserService,
                     private notification: NotificationService,
                     private logoutService: LogoutService) {
    this.currentUserObservable.subscribe(currentUser => this.currentUser = currentUser)
    this.classroomInfoObservable.subscribe(info => {
      this.classroomInfo = info
      this.logoutService.classroomInfo = info
    })
    this.userDisplayObservable.subscribe(users => this.users = users)
    this.conferencesObservable.subscribe(conferences =>
      this.conferences = conferences.filter(conf => conf.visible || this.currentUser?.userId === conf.creator!!.userId)
    )
    this.conferenceService.invitationEvents.subscribe(invitation => {
      this.handleInviteMsg(invitation)
    })
    this.join()
  }

  public isCurrentUserAuthorized(): boolean {
    if (this.currentUser === undefined) return false
    return Roles.isPrivileged(this.currentUser.userRole)
  }

  /**
   * @return True if service is connected to the backend.
   */
  public isConnected() {
    return this.rSocketService.isConnected()
  }

  public isSelf(user: UserCredentials) {
    return user.userId === this.currentUser?.userId
  }

  /**
   * Connect to backend
   * @return Observable that completes if connected.
   */
  public join() {
    return this.rSocketService.requestResponse<ClassroomInfo>("socket/init-classroom", "").pipe(
        tap(info => this.classroomInfoSubject.next(info))
      ).subscribe(
        classroom => this.notification.show("Connected to " + classroom.classroomName + "!"),
        e => {throw Error("Could not connect to classroom: \n" + e.message)}
    )
  }

  public createConference(conferenceInfo: ConferenceInfo) {
    this.conferenceService.createConference(conferenceInfo)
  }

  /**
   * Invites user to a conference.
   * @param invitee The user to invite
   * @param conferenceInfo
   * @param ticket
   */
  public inviteToConference(invitee: UserCredentials, conferenceInfo?: ConferenceInfo, ticket?: Ticket) {
    if (this.currentUser === undefined) throw new Error("Current user is undefined!")
    if (conferenceInfo !== undefined) {
      this.conferenceService.inviteToConference(invitee, this.currentUser, conferenceInfo)
    } else if (ticket !== undefined) {
      const conferenceInfo = this.findOrCreateConferenceOfTicket(ticket);
      this.conferenceService.inviteToConference(invitee, this.currentUser, conferenceInfo)
    } else if (this.currentUser?.conferences.length === 0) {
      const conferenceInfo = this.createConferenceInfo("Meeting", false)
      this.conferenceService.inviteToConference(invitee, this.currentUser, conferenceInfo)
    } else {
      this.dialog.open(InviteToConferenceDialogComponent, {
        height: 'auto',
        width: 'auto',
        data: this.currentUser
      }).beforeClosed().subscribe(conference => {
        if (this.currentUser === undefined) throw new Error("Current user is undefined!")
        if (conference !instanceof ConferenceInfo) throw new Error("Error in invite dialog!")
        this.conferenceService.inviteToConference(invitee, this.currentUser, conference)
      });
    }
  }

  private createConferenceInfo(conferenceName: string, visible: boolean = true, ticketId: number | null = null): ConferenceInfo {
    if (this.classroomInfo === undefined) throw new Error("ClassroomInfo is undefined!")
    if (this.currentUser === undefined) throw new Error("Current user is undefined!")
    const conferenceInfo = new ConferenceInfo()
    conferenceInfo.classroomId = this.classroomInfo.classroomId
    conferenceInfo.creator = this.currentUser
    conferenceInfo.visible = visible
    conferenceInfo.creationTimestamp = Date.now()
    conferenceInfo.conferenceName = conferenceName
    conferenceInfo.ticketId = ticketId
    return conferenceInfo
  }

  public chooseConferenceToJoin(conferencingUser: UserCredentials) {
    this.dialog.open(JoinUserConferenceDialogComponent, {
      height: 'auto',
      width: 'auto',
      data: conferencingUser
    }).beforeClosed().subscribe( conf => {
        if (conf !== undefined) {
          this.joinConference(conf)
        }
      })
  }

  public joinConference(conferenceInfo: ConferenceInfo) {
    this.conferenceService.joinConference(conferenceInfo)
  }

  public showUser() {
    this.userService.changeVisibility(true)
  }

  public hideUser() {
    this.userService.changeVisibility(false)
  }

  private handleInviteMsg(invitationEvent: InvitationEvent) {
    this.dialog.open(IncomingCallDialogComponent, {
        height: 'auto',
        width: 'auto',
        data: invitationEvent
      });
  }

  public createTicket() {
    this.dialog.open(NewTicketDialogComponent, {
      height: 'auto',
      width: 'auto',
    }).beforeClosed().pipe(
      filter(ticket => ticket),
      map((ticket: Ticket) => {
        if (this.currentUser === undefined) throw new Error("Current user is undefined!")
        ticket.classroomId = this.currentUser.classroomId
        ticket.creator = this.currentUser
        return ticket
      })
    ).subscribe((ticket: Ticket) => {
      this.ticketService.createTicket(ticket)
    });
  }

  public closeTicket(ticket: Ticket) {
    this.ticketService.removeTicket(ticket);
    this.notification.show(`Das Ticket wurde geschlossen`);
  }

  public isInConference(userCredentials?: UserCredentials): boolean {
    if (userCredentials === undefined) throw new Error("user is undefined!")
    let user: User | undefined
    if (userCredentials instanceof User)
      user = userCredentials
    else {
      user = this.users.find(userDisplay => userDisplay.userId === userCredentials.userId)
      if (user === undefined) return false
    }
    return user.conferences.length !== 0
  }

  public leave() {
    this.router.navigate(["/logout"], ).then()
  }

  private findOrCreateConferenceOfTicket(ticket: Ticket): ConferenceInfo {
    let conference = this.conferences.find(conference => conference.ticketId === ticket.ticketId)
    if (conference === undefined) {
      conference = this.createConferenceInfo(ticket.description, true, ticket.ticketId)
    }
    return conference
  }
}
