import {Injectable} from '@angular/core';
import {Observable, Subject, BehaviorSubject, Subscription} from 'rxjs';
import {AuthService} from './auth.service';
import {MatDialog} from '@angular/material/dialog';
import {IncomingCallDialogComponent} from '../dialogs/incoming-call-dialog/incoming-call-dialog.component';
import {User} from "../model/User";
import {ConferenceService} from "./conference.service";
import {RSocketService} from "../rsocket/r-socket.service";
import {ClassroomInfo} from "../model/ClassroomInfo";
import {Ticket} from "../model/Ticket";
import {TicketService} from "./ticket.service";
import {filter, map} from "rxjs/operators";
import {UserService} from "./user.service";
import {Roles} from "../model/Roles";
import {NewTicketDialogComponent} from "../dialogs/newticket-dialog/new-ticket-dialog.component";

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
  private users: User[] = []

  private classroomInfoSubject: Subject<ClassroomInfo> = new BehaviorSubject(new ClassroomInfo())
  classroomInfo: Observable<ClassroomInfo> = this.classroomInfoSubject.asObservable()

  private selfSubject: Subject<User> = new BehaviorSubject(this.authService.getToken())
  currentUserObservable: Observable<User> = this.selfSubject.asObservable()
  currentUser: User

  private usersInConference: Subject<User[]>;
  private inviteUsers: Subject<boolean>;

  public constructor(private authService: AuthService,
                     private conferenceService: ConferenceService,
                     private dialog: MatDialog,
                     private rSocketService: RSocketService,
                     private ticketService: TicketService,
                     private userService: UserService) {
    this.usersInConference = new BehaviorSubject<User[]>([]);
    this.inviteUsers = new Subject<boolean>();
    this.currentUserObservable.subscribe(currentUser => this.currentUser = currentUser)
    this.userObservable.subscribe(users => this.users = users)
  }

  public isCurrentUserAuthorized(): boolean {
    return Roles.isPrivileged(this.currentUser.userRole)
  }

  public userInviter(): Observable<boolean> {
    return this.inviteUsers.asObservable();
  }

  /**
   * @return True if service is connected to the backend.
   */
  public isJoined() {
    return this.rSocketService.isConnected()
  }

  public isNotSelf(user: User) {
    return user.userId != this.currentUser.userId
  }

  /**
   * Connect to backend
   * @return Observable that completes if connected.
   */
  public join() {
    return this.rSocketService
      .requestResponse<ClassroomInfo>("socket/init-classroom", "")
  }

  /**
   * Invites user to a conference by following the link provided as href.
   * @param users The users to invite
   */
  public inviteToConference(users: User[]) {
  }

  private handleInviteMsg() {
    const body = JSON.parse("");
    this.dialog.open(IncomingCallDialogComponent, {
      height: 'auto',
      width: 'auto',
      data: {inviter: body.user, cid: body.cid}
    });

  }

  private handleUsersMsg() {
  }

  private handleTicketsMsg() {
  }

  private joinCourse() {
  }

  private constructHeaders() {
    return {'Auth-Token': this.authService.loadToken()};
  }

  private send(topic: string, body: {}): void {
    //this.stompRx.send(topic, body, this.constructHeaders());
  }

  //private listen(topic: string): Observable<Message> {
    //return this.stompRx.subscribeToTopic(topic, this.constructHeaders());
  //}

  public openConference() {
    this.conferenceService.createConference()
  }

  private handleConferenceUsersMsg() {
    this.usersInConference.next(JSON.parse(""));
  }

  public joinConference(user: User, mid: number = 0) {
  }

  public showConference() {
  }

  public hideConference() {
  }

  public showUser() {
  }

  public hideUser() {
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

  public isInConference(user: User) {
    return false
    //return this.usersInConference.filter(u => u.userId === user.userId).length !== 0;
  }

  public isInConferenceId(userId: string) {
    return false
    //return this.usersInConference.filter(u => u.userId === userId).length !== 0;
  }
  public isInClassroom(userId: string) {
    return this.users.filter(u => u.userId === userId).length !== 0;
  }

}
