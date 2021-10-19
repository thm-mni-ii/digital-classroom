import {Component, Input, OnInit} from '@angular/core';
import {UserIconService} from "../../../../../util/user-icon.service";
import {User} from "../../../../../model/User";
import {Ticket} from "../../../../../model/Ticket";
import {UserService} from "../../../../../service/user.service";
import {TicketService} from "../../../../../service/ticket.service";
import {NotificationService} from "../../../../../service/notification.service";
import {ClassroomService} from "../../../../../service/classroom.service";

@Component({
  selector: 'app-ticket-assign',
  templateUrl: './ticket-assign.component.html',
  styleUrls: ['./ticket-assign.component.scss']
})
export class TicketAssignComponent implements OnInit {

  @Input() ticket?: Ticket;
  @Input() label?: string;
  @Input() users: User[] = []
  assigneeId: string;

  constructor(
    public userIconService: UserIconService,
    private ticketService: TicketService,
    public userService: UserService,
    private notification: NotificationService,
    public classroomService: ClassroomService
  ) {
    this.assigneeId = "†none"
  }

  ngOnInit(): void {
    if (this.ticket!!.assignee !== null && this.ticket!!.assignee !== undefined) {
      this.assigneeId = this.ticket!!.assignee!!.userId
    } else {
      this.assigneeId = "†none"
    }
  }

  public assignUser() {
    console.log(this.assigneeId);
    const newAssignee = this.fullUser(this.assigneeId)
    this.ticket!!.assignee = newAssignee
    this.ticketService.updateTicket(this.ticket!!);
    if (this.ticket!!.assignee === undefined) {
      this.notification.show("Der Bearbeiter wurde vom Ticket entfernt.")
    } else {
      this.notification.show(`${newAssignee!!.fullName} wurde dem Ticket als Bearbeiter zugewiesen`)
    }
  }

  public fullUser(userId: string): User | undefined {
    return this.userService.getFullUser(userId)
  }

}
