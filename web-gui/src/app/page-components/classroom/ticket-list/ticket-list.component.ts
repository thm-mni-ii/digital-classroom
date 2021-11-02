import {Component, Input} from '@angular/core';
import {Ticket} from "../../../model/Ticket";
import {ClassroomService} from "../../../service/classroom.service";
import {User, UserCredentials} from "../../../model/User";
import {Howl} from "howler";
import {AssetManagerService} from "../../../util/asset-manager.service";

@Component({
  selector: 'app-ticket-list',
  templateUrl: './ticket-list.component.html',
  styleUrls: ['./ticket-list.component.scss']
})
export class TicketListComponent {

  @Input() currentUser?: UserCredentials
  @Input() tickets: Ticket[] = []
  @Input() users: User[] = []

  playSound: boolean = false;
  private sound = new Howl({
    src: [this.assetManager.getAsset("notification")],
    loop: false,
    volume: 0.5
  })

  constructor(
    public classroomService: ClassroomService,
    private assetManager: AssetManagerService
  ) {
    this.classroomService.newTicketObservable.subscribe(
      ticket => this.playSoundOnNewTicket(ticket)
    )
  }

  private playSoundOnNewTicket(ticket: Ticket) {
    if (!this.playSound) return
    const notification: Notification = new Notification('Neues Ticket',
      {body: ticket.description + ' von ' + ticket.creator!!.fullName});
    notification.onclick = () => window.focus();

    this.sound.play()
  }

  public sortTickets(tickets: Ticket[]) {
    if (tickets.length <= 0) return null
    return tickets.sort( (a, b) => {
      if (a.assignee?.userId === this.currentUser!!.userId && b.assignee?.userId === this.currentUser!!.userId) {
        return a.createTime > b.createTime ? 1 : -1;
      } else if (a.assignee?.userId === this.currentUser!!.userId) {
        return -1;
      } else if (b.assignee?.userId === this.currentUser!!.userId) {
        return 1;
      }
      return a.createTime > b.createTime ? 1 : -1;
    });
  }

}
