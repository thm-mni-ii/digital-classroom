import {Injectable} from '@angular/core';
import {Observable, Subject, BehaviorSubject, Subscription} from 'rxjs';
import {AuthService} from './auth.service';
import {MatDialog} from '@angular/material/dialog';
import {IncomingCallDialogComponent} from '../dialogs/incoming-call-dialog/incoming-call-dialog.component';
import {User} from "../model/User";
import {HttpClient} from "@angular/common/http";
import {ConferenceService} from "./conference.service";
import {RSocketService} from "./r-socket.service";

/**
 * Service that provides observables that asynchronously updates tickets, users and privide Conferences to take
 * part in a conference.
 */
@Injectable({
  providedIn: 'root'
})
export class ClassroomService {
  private dialog: MatDialog;
  private usersInConference: Subject<User[]>;
  private inviteUsers: Subject<boolean>;
  private service = 'bigbluebutton';
  incomingCallSubscriptions: Subscription[] = [];
  private heartbeatInterval: number;
  private heartbeatTime = 5000;
  private self: User;

  public constructor(private authService: AuthService,
                     private conferenceService: ConferenceService,
                     private mDialog: MatDialog,
                     private http: HttpClient,
                     private socketService: RSocketService) {
    this.usersInConference = new BehaviorSubject<User[]>([]);
    this.inviteUsers = new Subject<boolean>();
    this.dialog = mDialog;
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

  private handleInviteMsg() {
    const body = JSON.parse("");
    this.dialog.open(IncomingCallDialogComponent, {
      height: 'auto',
      width: 'auto',
      data: {inviter: body.user, cid: body.cid}
    });

  }

  private handleUsersMsg() {
    //this.users.next(JSON.parse(msg.body));
  }

  private handleTicketsMsg() {
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
    this.conferenceService.createConference()
  }

  private requestConferenceUsersUpdate() {
    this.requestUsersUpdate();
    this.send('/websocket/classroom/conference/users', {courseId: this.self.classroomId});
  }

  private handleConferenceUsersMsg() {
    this.usersInConference.next(JSON.parse(""));
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
