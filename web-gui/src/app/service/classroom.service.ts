import {Injectable} from '@angular/core';
import {Observable, Subject, BehaviorSubject, Subscription} from 'rxjs';
import {distinctUntilChanged} from 'rxjs/operators';
import {Message} from 'stompjs';
import {BbbConferenceHandlingService} from './bbb-conference-handling.service';
import {AuthService} from './auth.service';
import {MatDialog} from '@angular/material/dialog';
import {IncomingCallDialogComponent} from '../dialogs/incoming-call-dialog/incoming-call-dialog.component';
import {Ticket} from '../model/Ticket';
import {User} from "../model/User";
import {HttpClient} from "@angular/common/http";

/**
 * Service that provides observables that asynchronacally updates tickets, users and privide Conferences to take
 * part in a conference.
 */
@Injectable({
  providedIn: 'root'
})
export class ClassroomService {
  private dialog: MatDialog;
  private users: Observable<User>;
  private tickets: Observable<Ticket[]>;
  private usersInConference: Subject<User[]>;
  private inviteUsers: Subject<boolean>;
  private conferenceWindowHandle: Window;
  private isWindowhandleOpen: Subject<Boolean>;
  private service = 'bigbluebutton';
  incomingCallSubscriptions: Subscription[] = [];
  private heartbeatInterval: number;
  private heartbeatTime = 5000;
  private self: User;

  public constructor(private authService: AuthService,
                     private conferenceService: BbbConferenceHandlingService,
                     private mDialog: MatDialog,
                     private http: HttpClient) {
    this.users = http.get<User>("/classroom-api/users")
    this.tickets = http.get<Ticket[]>("/classroom-api/ticket")
    this.isWindowhandleOpen = new Subject<Boolean>();
    this.isWindowhandleOpen.asObservable().pipe(distinctUntilChanged()).subscribe((isOpen) => {
        if (!isOpen) {
          this.closeConference();
        }
    });
    this.isWindowhandleOpen.next(true);
    this.usersInConference = new BehaviorSubject<User[]>([]);
    this.inviteUsers = new Subject<boolean>();
    this.conferenceService.getSelectedConferenceSystem().subscribe((service: string) => {
      this.service = service;
    });
    this.dialog = mDialog;
    // this.conferenceWindowHandle = new Window();
    setInterval(() => {
      if (this.conferenceWindowHandle) {
        if (this.conferenceWindowHandle.closed) {
          this.isWindowhandleOpen.next(false);
        } else {
          this.isWindowhandleOpen.next(true);
        }
      }
    }, 1000);
  }

  /**
   * @return Users of the connected course.
   */
  public getUsers(): Observable<User> {
    return this.users;
  }

  public getConferenceWindowHandle() {
    return this.isWindowhandleOpen.asObservable();
  }
  /**
   * @return Users in public conferences.
   */
  public getUsersInConference(): Observable<User[]> {
    return this.usersInConference.asObservable();
  }

  public userInviter(): Observable<boolean> {
    return this.inviteUsers.asObservable();
  }

  /**
   * @return Tickets of the connected course.
   */
  public getTickets(): Observable<Ticket[]> {
    return this.tickets;
  }

  /**
   * @return True if service is connected to the backend.
   */
  public isJoined() {
    return true
  }

  /**
   * Connect to backend
   * @return Observable that completes if connected.
   * @param self the user joined in this classroom.
   */
  public join(self: User) {
    this.self = self
    //this.stompRx = new RxStompClient(
    //  window.origin.replace(/^http(s)?/, 'ws$1') + '/websocket', this.constructHeaders()
    //);
     //this.stompRx.onConnect(_ => {
    //   // Handles Conference from tutors / docents to take part in a webconference
    //   this.listen('/user/' + this.authService.getToken().id + '/classroom/invite').subscribe(m => this.handleInviteMsg(m));
    //   this.listen('/user/' + this.authService.getToken().id + '/classroom/users').subscribe(m => this.handleUsersMsg(m));
    //   this.listen('/topic/classroom/' + this.courseId + '/left').subscribe(_m => this.requestUsersUpdate());
    //   this.listen('/topic/classroom/' + this.courseId + '/joined').subscribe(_m => this.requestUsersUpdate());
    //
    //   this.listen('/user/' + this.authService.getToken().id + '/classroom/tickets').subscribe(m => this.handleTicketsMsg(m));
    //   this.listen('/topic/classroom/' + this.courseId + '/ticket/create').subscribe(_m => this.requestTicketsUpdate());
    //   this.listen('/topic/classroom/' + this.courseId + '/ticket/update').subscribe(_m => this.requestTicketsUpdate());
    //   this.listen('/topic/classroom/' + this.courseId + '/ticket/remove').subscribe(_m => this.requestTicketsUpdate());
    //
    //   this.listen('/topic/classroom/' + this.courseId + '/conference/opened').subscribe(_m => this.requestConferenceUsersUpdate());
    //   this.listen('/user/' + this.authService.getToken().id + '/classroom/opened').subscribe(m => this.handleConferenceOpenedMsg(m));
    //   this.listen('/topic/classroom/' + this.courseId + '/conference/closed').subscribe(_m => this.requestConferenceUsersUpdate());
    //   this.listen('/user/' + this.authService.getToken().id + '/classroom/conference/users')
    //     .subscribe(m => this.handleConferenceUsersMsg(m));
    //   this.listen('/user/' + this.authService.getToken().id + '/classroom/conference/joined')
    //     .subscribe(m => this.handleConferenceJoinedMsg(m));
    //   this.joinCourse();
    //   this.requestUsersUpdate();
    //   this.requestConferenceUsersUpdate();
    //   this.requestTicketsUpdate();
    //
    //   // this.heartbeatInterval = window.setInterval(()=>{
    //   //  this.send('/websocket/classroom/heartbeat', {});
    //   //}, this.heartbeatTime)
    //
    //});
    //this.stompRx.connect();

  }

  /**
   * Disconnects from the endpoint.
   * @return Observable that completes when disconnected.
   */
  public leave() {
    this.send('/websocket/classroom/leave', {courseId: this.self.classroomId});
    clearInterval(this.heartbeatInterval);
    return
  }

  /**
   * Invites user to a conference by following the link provided as href.
   * @param users The users to invite
   */
  public inviteToConference(users: User[]) {
    this.send('/websocket/classroom/conference/invite', {users: users, 'courseid': this.self.classroomId});
  }

  /**
   * Creates a new ticket.
   * @param ticket The ticket to create.
   */
  public createTicket(ticket: Ticket) {
    this.http.post<Ticket[]>("/classroom-api/ticket", ticket).subscribe()
  }

  /**
   * Updates an existing ticket.
   * @param ticket The ticket to update.
   */
  public updateTicket(ticket: Ticket) {
    this.http.put<Ticket[]>("/classroom-api/ticket", ticket).subscribe()
  }

  /**
   * Removes an existing ticket.
   * @param ticket The ticket to remove.
   */
  public removeTicket(ticket: Ticket) {
    ticket.queuePosition = null;
    this.http.post<Ticket[]>("/classroom-api/ticket/delete", ticket).subscribe()
  }

  private handleInviteMsg(msg: Message) {
    const body = JSON.parse(msg.body);
    this.dialog.open(IncomingCallDialogComponent, {
      height: 'auto',
      width: 'auto',
      data: {inviter: body.user, cid: body.cid}
    });

  }

  private handleUsersMsg(msg: Message) {
    //this.users.next(JSON.parse(msg.body));
  }

  private handleTicketsMsg(msg: Message) {
    //this.tickets.next(JSON.parse(msg.body));
  }

  private requestUsersUpdate() {
    this.send('/websocket/classroom/users', {courseId: this.self.classroomId});
  }

  private requestTicketsUpdate() {
    this.send('/websocket/classroom/tickets', {courseId: this.self.classroomId});
  }

  private joinCourse() {
    this.send('/websocket/classroom/join', {courseId: this.self.classroomId});
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
    this.send('/websocket/classroom/conference/open', {service: this.service, courseId: this.self.classroomId});
  }

  public closeConference() {
    if (this.conferenceWindowHandle && !this.conferenceWindowHandle.closed) {
      this.conferenceWindowHandle.close();
    }
    this.send('/websocket/classroom/conference/close', {});
  }

  private requestConferenceUsersUpdate() {
    this.requestUsersUpdate();
    this.send('/websocket/classroom/conference/users', {courseId: this.self.classroomId});
  }

  private handleConferenceUsersMsg(msg: Message) {
    this.usersInConference.next(JSON.parse(msg.body));
  }

  private handleConferenceOpenedMsg(msg: Message) {
    this.inviteUsers.next(true);
    this.conferenceWindowHandle = window.open(JSON.parse(msg.body).href);
  }

  private handleConferenceJoinedMsg(msg: Message) {
    this.conferenceWindowHandle = window.open(JSON.parse(msg.body).href);
  }

  public joinConference(user: User, mid: number = 0) {
    this.send('/websocket/classroom/conference/join', {user: user, mid: mid, courseId: this.self.classroomId});
  }

  public showConference() {
    this.send('/websocket/classroom/conference/show', {});
  }

  public hideConference() {
    this.send('/websocket/classroom/conference/hide', {});
  }

  public showUser() {
    this.send('/websocket/classroom/user/show', {courseId: this.self.classroomId});
  }

  public hideUser() {
    this.send('/websocket/classroom/user/hide', {courseId: this.self.classroomId});
  }
}
