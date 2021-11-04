import { Component, Input, OnInit } from '@angular/core';
import { Ticket } from '../../../../model/Ticket';
import { TimeFormatterService } from '../../../../util/time-formatter.service';
import { ClassroomService } from '../../../../service/classroom.service';
import { ConferenceInfo } from '../../../../model/ConferenceInfo';
import { User, UserCredentials } from '../../../../model/User';
import { UserService } from '../../../../service/user.service';
import {tap} from "rxjs/operators";

@Component({
  selector: 'app-ticket',
  templateUrl: './ticket.component.html',
  styleUrls: ['./ticket.component.scss'],
})
export class TicketComponent implements OnInit {
  @Input() ticket?: Ticket;
  @Input() users: User[] = [];
  conference?: ConferenceInfo;

  constructor(
    private timeFormatterService: TimeFormatterService,
    public classroomService: ClassroomService,
    public userService: UserService
  ) {}

  ngOnInit(): void {
    if (this.ticket === undefined) throw new Error('Ticket is undefined!');
    if (this.ticket?.creator === undefined)
      throw new Error('Ticket ' + this.ticket?.ticketId + ' without creator');
  }

  getTicketTimeAgo(ticket: Ticket): string {
    return this.timeFormatterService.timeAgo(ticket.createTime);
  }

  getTicketTime(ticket: Ticket): string {
    return this.timeFormatterService.format(ticket.createTime);
  }

  public determineButton(): 'join' | 'invite' | 'none' {
    this.conference = this.classroomService.findConferenceOfTicket(this.ticket!!);
    if (this.conference !== undefined) return 'join';
    else if (!this.classroomService.isSelf(this.ticket?.creator!!)) return 'invite'
    else return 'none';
  }

  public mayDeleteTicket(): boolean {
    return (
      this.classroomService.isSelf(this.ticket?.creator!!) ||
      this.classroomService.isCurrentUserPrivileged()
    );
  }

  public mayEditTicket(): boolean {
    return this.classroomService.isSelf(this.ticket?.creator!!);
  }

  editTicket() {
    this.classroomService.createOrEditTicket(this.ticket);
  }

  closeTicket() {
    this.classroomService.closeTicket(this.ticket!!);
  }

  inviteCreator() {
    this.classroomService
      .createNewConferenceForTicket(this.ticket!!).pipe(
        tap(conf => this.classroomService.updateTicketWithConference(this.ticket!!, conf.conferenceId))
      ).subscribe((conf) =>
        this.classroomService.inviteToConference(this.ticket!!.creator, conf)
      );
  }

  linkConference() {
    this.classroomService.linkTicketToConference(this.ticket!!);
  }

  joinConference() {
    this.classroomService.conferenceService.joinConference(this.conference!!);
  }

  public fullUser(user: UserCredentials): User | undefined {
    return this.userService.getFullUser(user.userId);
  }
}
