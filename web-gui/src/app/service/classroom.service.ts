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
import {CreateEditTicketComponent, TicketEditData} from "../dialogs/create-edit-ticket/create-edit-ticket.component";
import {ConferenceInfo} from "../model/ConferenceInfo";
import {ClassroomInfo} from "../model/ClassroomInfo";
import {InviteToConferenceDialogComponent} from "../dialogs/invite-to-conference-dialog/invite-to-conference-dialog.component";
import {Router} from "@angular/router";
import {NotificationService} from "./notification.service";
import {JoinUserConferenceDialogComponent} from "../dialogs/join-user-conference-dialog/join-user-conference-dialog.component";
import {LogoutService} from "./logout.service";
import {
  CreateConferenceDialogComponent,
  CreateConferenceInputData
} from "../dialogs/create-conference-dialog/create-conference-dialog.component";
import {
  LinkConferenceInputData,
  LinkConferenceToTicketDialogComponent
} from "../dialogs/link-conference-to-ticket-dialog/link-conference-to-ticket-dialog.component";
import {ConfirmationDialogComponent} from "../dialogs/confirmation-dialog/confirmation-dialog.component";
import {InvitationEvent} from "../rsocket/event/InvitationEvent";

/**
 * Service that provides observables that asynchronously updates tickets, users and
 * provide Conferences to take part in a conference.
 */
@Injectable({
  providedIn: 'root'
})
export class ClassroomService {

  public newTicketObservable = this.ticketService.newTicketObservable.pipe(
    filter(ticket => ticket.creator.userId !== this.currentUser!!.userId)
  )
  public tickets = this.ticketService.ticketObservable
  public userDisplayObservable = this.userService.userObservable
  public currentUserObservable = this.userService.currentUserObservable
  public conferencesObservable = this.conferenceService.conferencesObservable.pipe(
    map(conferences =>
      conferences.filter(
        conf => this.isCurrentUserPrivileged() || (
            conf.visible ||
            conf.creator!!.userId == this.currentUser?.userId!! ||
            conf.attendeeIds.includes(this.currentUser?.userId!!)
          )
      )
    )
  )

  private conferences: ConferenceInfo[] = []
  private myConferences: ConferenceInfo[] = []

  private users: User[] = []

  private classroomInfoSubject: Subject<ClassroomInfo> = new BehaviorSubject(new ClassroomInfo())
  classroomInfoObservable: Observable<ClassroomInfo> = this.classroomInfoSubject.asObservable()

  public classroomInfo: ClassroomInfo | undefined
  public currentUser: User | undefined

  public constructor(
    private router: Router,
    private authService: AuthService,
    public conferenceService: ConferenceService,
    private dialog: MatDialog,
    private rSocketService: RSocketService,
    public ticketService: TicketService,
    public userService: UserService,
    private notification: NotificationService,
    private logoutService: LogoutService
  ) {
    this.connectToBackend()
  }

  /**
   * Connect to backend
   * @return Observable that completes if connected.
   */
  public connectToBackend() {
    this.rSocketService.requestResponse<ClassroomInfo>("socket/init-classroom", "").pipe(
      tap(info => this.classroomInfoSubject.next(info))
    ).subscribe(
      classroom => this.notification.show("Connected to " + classroom.classroomName + "!"),
      e => {throw Error("Could not connect to classroom: \n" + e.message)}
    )
    this.currentUserObservable.subscribe(currentUser => this.currentUser = currentUser)
    this.classroomInfoObservable.subscribe(info => {
      this.classroomInfo = info
      this.logoutService.classroomInfo = info
    })
    this.userDisplayObservable.subscribe(users => this.users = users)
    this.conferencesObservable.subscribe(conferences => {
      this.conferences = conferences
      this.myConferences = conferences.filter( conf =>
        conf.creator!!.userId == this.currentUser?.userId!! ||
        conf.attendeeIds.includes(this.currentUser?.userId!!)
      )
    })
    this.conferenceService.invitationEvents.subscribe(invitation => this.handleInviteMsg(invitation))
  }

  public isCurrentUserPrivileged(): boolean {
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

  public configureConference(
    conferenceName: string,
    visible: boolean = true,
  ): ConferenceInfo {
    const conferenceInfo = new ConferenceInfo()
    conferenceInfo.classroomId = this.classroomInfo!!.classroomId
    conferenceInfo.creator = this.currentUser!!
    conferenceInfo.visible = visible
    conferenceInfo.creationTimestamp = Date.now()
    conferenceInfo.conferenceName = conferenceName
    return conferenceInfo
  }

  /**
   * Invites user to a conference.
   * @param invitee The user to invite
   * @param conferenceInfo
   */
  public inviteToConference(invitee: UserCredentials, conferenceInfo?: ConferenceInfo) {
    if (this.currentUser === undefined) throw new Error("Current user is undefined!")
    if (conferenceInfo !== undefined) {
      this.conferenceService.inviteToConference(invitee, this.currentUser, conferenceInfo)
    } else if (this.currentUser?.conferences.length === 0) {
      const conferenceInfo = this.configureConference(
        "Meeting", false)
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

  public chooseConferenceOfUser(conferencingUser: UserCredentials = this.currentUser!!): Observable<ConferenceInfo> {
    return this.dialog.open(JoinUserConferenceDialogComponent, {
      height: 'auto',
      width: 'auto',
      data: conferencingUser
    }).beforeClosed()
  }

  private handleInviteMsg(invitationEvent: InvitationEvent) {
    this.dialog.open(IncomingCallDialogComponent, {
        height: 'auto',
        width: 'auto',
        data: invitationEvent
      }).beforeClosed().subscribe((acceptCall: boolean) => {
        if (acceptCall) this.conferenceService.joinConference(invitationEvent.conferenceInfo!!)
    });
  }

  public createOrEditTicket(originalTicket?: Ticket) {
    this.dialog.open(CreateEditTicketComponent, {
      height: 'auto',
      width: 'auto',
      data: new TicketEditData(this.currentUser!!, this.myConferences, originalTicket)
    }).beforeClosed().pipe(
      filter(ticket => ticket),
      map((ticket: Ticket) => {
        if (this.currentUser === undefined) throw new Error("Current user is undefined!")
        ticket.classroomId = this.currentUser.classroomId
        ticket.creator = this.currentUser
        return ticket
      })
    ).subscribe((ticket: Ticket) => {
      if (ticket.ticketId === 0) {
        this.ticketService.createTicket(ticket)
      } else {
        this.ticketService.editTicket(ticket)
      }
    });
  }

  public closeTicket(ticket: Ticket) {
    this.ticketService.removeTicket(ticket);
    this.notification.show(`Das Ticket wurde geschlossen`);
  }

  public isInConference(userCredentials: UserCredentials): boolean {
    return this.conferenceService.isUserInConference(userCredentials)
  }

  public createNewConferenceForTicket(ticket: Ticket): Observable<ConferenceInfo> {
    const info = this.configureNewConferenceForTicket(ticket)
    return this.conferenceService.createConference(info)
  }

  public configureNewConferenceForTicket(ticket: Ticket): ConferenceInfo {
    return this.configureConference(ticket.description, true)
  }

  public logout() {
    this.router.navigate(["/logout"], ).then()
  }

  public createConference(plenary: boolean = false) {
    this.dialog.open(CreateConferenceDialogComponent, {
      height: 'auto',
      width: 'auto',
      data: new CreateConferenceInputData(this.classroomInfo!!, this.currentUser!!, plenary)
    }).beforeClosed().pipe(
      filter(conferenceInfo => conferenceInfo instanceof ConferenceInfo),
    ).subscribe((conferenceInfo: ConferenceInfo) => {
      this.conferenceService.createConference(conferenceInfo)
    });
  }

  public linkTicketToConference(ticket: Ticket) {
    this.dialog.open(LinkConferenceToTicketDialogComponent, {
      height: 'auto',
      width: 'auto',
      data: new LinkConferenceInputData(this.myConferences, ticket)
    }).beforeClosed().pipe(
      filter(conferenceId => conferenceId !== undefined), // undefined = do not update.
      tap((conferenceId: string | null) => this.updateTicketWithConference(ticket, conferenceId))
    ).subscribe()
  }

  updateTicketWithConference(ticket: Ticket, conferenceId: string | null) {
    if (conferenceId === undefined) ticket.conferenceId = null
    else ticket.conferenceId = conferenceId
    this.ticketService.editTicket(ticket)
  }

  public findTicketOfConference(conferenceInfo: ConferenceInfo): Ticket | null {
    return this.ticketService.getTicketOfConference(conferenceInfo)
  }

  public findConferenceOfTicket(ticket: Ticket): ConferenceInfo | undefined {
    return this.conferences.find(conference => conference.conferenceId === ticket.conferenceId)
  }

  getConfirmation(question: string): Observable<boolean> {
    return this.dialog.open(ConfirmationDialogComponent, {
      height: 'auto',
      width: 'auto',
      data: question
    }).beforeClosed()
  }
}
