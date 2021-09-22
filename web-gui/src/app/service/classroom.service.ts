import {Injectable} from '@angular/core';
import {Observable, Subject, BehaviorSubject} from 'rxjs';
import {AuthService} from './auth.service';
import {MatDialog} from '@angular/material/dialog';
import {IncomingCallDialogComponent} from '../dialogs/incoming-call-dialog/incoming-call-dialog.component';
import {User, UserDisplay} from "../model/User";
import {ConferenceService} from "./conference.service";
import {RSocketService} from "../rsocket/r-socket.service";
import {Ticket} from "../model/Ticket";
import {TicketService} from "./ticket.service";
import {filter, map, tap} from "rxjs/operators";
import {UserService} from "./user.service";
import {Roles} from "../model/Roles";
import {NewTicketDialogComponent} from "../dialogs/new-ticket-dialog/new-ticket-dialog.component";
import {ConferenceInfo} from "../model/ConferenceInfo";
import {ClassroomInfo} from "../model/ClassroomInfo";
import {InvitationEvent} from "../rsocket/event/ClassroomEvent";

/**
 * Service that provides observables that asynchronously updates tickets, users and privide Conferences to take
 * part in a conference.
 */
@Injectable({
  providedIn: 'root'
})
export class ClassroomService {

  public tickets = this.ticketService.ticketObservable
  public userObservable = this.userService.userObservable
  public currentUserObservable = this.userService.currentUserObservable
  public conferencesObservable = this.conferenceService.conferencesObservable
  public attendedConferencesObservable = this.conferenceService.attendedConferencesObservable
  private users: UserDisplay[] = []

  private classroomInfoSubject: Subject<ClassroomInfo> = new BehaviorSubject(new ClassroomInfo())
  classroomInfoObservable: Observable<ClassroomInfo> = this.classroomInfoSubject.asObservable()

  public classroomInfo: ClassroomInfo
  public currentUser: UserDisplay

  public constructor(private authService: AuthService,
                     private conferenceService: ConferenceService,
                     private dialog: MatDialog,
                     private rSocketService: RSocketService,
                     private ticketService: TicketService,
                     private userService: UserService) {
    this.userService.currentUserObservable.subscribe(
      currentUser => this.currentUser = currentUser
    )
    this.classroomInfoObservable.subscribe(info => this.classroomInfo = info)
    this.userObservable.subscribe(users => this.users = users)
    this.conferenceService.invitationEvents.subscribe(invitation => {
      this.handleInviteMsg(invitation)
    })
    this.join()
  }

  public isCurrentUserAuthorized(): boolean {
    return Roles.isPrivileged(this.currentUser.userRole)
  }

  /**
   * @return True if service is connected to the backend.
   */
  public isJoined() {
    return this.rSocketService.isConnected()
  }

  public isSelf(user: User) {
    return user.userId === this.currentUser.userId
  }

  /**
   * Connect to backend
   * @return Observable that completes if connected.
   */
  public join() {
    return this.rSocketService.requestResponse<ClassroomInfo>("socket/init-classroom", "").pipe(
        tap(info => this.classroomInfoSubject.next(info))
      ).subscribe()
  }

  public createConference(conferenceInfo: ConferenceInfo) {
    this.conferenceService.createConference(conferenceInfo)
  }

  /**
   * Invites user to a conference.
   * @param user The user to invite
   * @param conferenceInfo
   * @param ticket
   */
  public inviteToConference(user: User, conferenceInfo: ConferenceInfo = null, ticket: Ticket = null) {
    if (conferenceInfo !== null) {
      this.conferenceService.inviteToConference(user, conferenceInfo)
    } else if (ticket !== null) {
      const conferenceInfo = new ConferenceInfo()
      conferenceInfo.classroomId = this.classroomInfo.classroomId
      conferenceInfo.creator = this.currentUser
      conferenceInfo.visible = true
      conferenceInfo.creation = Date.now()
      conferenceInfo.conferenceName = ticket.description
      this.conferenceService.inviteToConference(user, conferenceInfo)
    } else {
      throw new Error("No ticket or conference provided for invitation!")
    }
  }

  public joinConferenceOfUser(conferencingUser: User) {
    this.conferenceService.joinConferenceOfUser(conferencingUser)
  }

  public joinConference(conferenceInfo: ConferenceInfo) {
    this.conferenceService.joinConference(conferenceInfo)
  }

  public showConference() {
  }

  public hideConference() {
  }

  public showUser() {
  }

  public hideUser() {
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
        ticket.classroomId = this.currentUser.classroomId
        ticket.creator = this.currentUser
        return ticket
      })
    ).subscribe((ticket: Ticket) => {
      this.ticketService.createTicket(ticket)
    });
  }

  public isInConference(userId: string): boolean {
    const userDisplay = this.users.find(userDisplay => userDisplay.userId === userId)
    if (userDisplay !== undefined) {
      return userDisplay.inConference
    } else {
      return false
    }
  }

}
