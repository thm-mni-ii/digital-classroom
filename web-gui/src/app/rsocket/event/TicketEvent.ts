import {Ticket} from "../../model/Ticket";
import {ClassroomEvent} from "./ClassroomEvent";

export enum TicketAction {
  CREATE = "CREATE",
  ASSIGN = "ASSIGN",
  CLOSE = "CLOSE"
}

export class TicketEvent extends ClassroomEvent {
    ticket: Ticket
    ticketAction: TicketAction

    constructor(ticket: Ticket, action: TicketAction) {
      super("TicketEvent");
      this.ticket = ticket
      this.ticketAction = action
    }
}
