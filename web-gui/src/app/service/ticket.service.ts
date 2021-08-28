import {Injectable, OnInit} from '@angular/core';
import {Ticket} from "../model/Ticket";
import {RSocketService} from "../rsocket/r-socket.service";
import {BehaviorSubject, Observable, Subject} from "rxjs";
import {TicketAction, TicketEvent} from "../rsocket/event/TicketEvent";
import {EventListenerService} from "../rsocket/event-listener.service";
import {map} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class TicketService {

  private tickets: Ticket[] = []
  private ticketSubject: Subject<Ticket[]> = new BehaviorSubject([])
  ticketObservable: Observable<Ticket[]> = this.ticketSubject.asObservable()

  constructor(
    private rSocketService: RSocketService,
    private eventListenerService: EventListenerService
  ) {
    this.initTickets()
    this.eventListenerService.ticketEvents.pipe(
      map((ticketEvent: TicketEvent) => this.handleTicketEvent(ticketEvent)),
      map(() => this.publish())
    ).subscribe()
  }

  private initTickets() {
    this.rSocketService.requestStream<Ticket>("socket/init-tickets", "Init Tickets").pipe(
      map(ticket => this.tickets.push(ticket)),
      map(() => this.publish())
    ).subscribe()
  }

  private handleTicketEvent(ticketEvent: TicketEvent) {
    switch (ticketEvent.ticketAction) {
      case TicketAction.CREATE: {
        this.tickets.push(ticketEvent.ticket)
        break;
      }
      case TicketAction.ASSIGN: {
        const index = this.tickets.map(ticket => ticket.ticketId).indexOf(ticketEvent.ticket.ticketId)
        this.tickets[index] = ticketEvent.ticket
        break;
      }
      case TicketAction.CLOSE: {
        const index = this.tickets.map(ticket => ticket.ticketId).indexOf(ticketEvent.ticket.ticketId)
        this.tickets.splice(index, 1)
        break;
      }
    }
  }

  private publish() {
    this.ticketSubject.next(this.tickets)
  }

  /**
   * Creates a new ticket.
   * @param ticket The ticket to create.
   */
  public createTicket(ticket: Ticket) {
    const ticketEvent = new TicketEvent()
    ticketEvent.ticket = ticket
    ticketEvent.ticketAction = TicketAction.CREATE
    return this.rSocketService.fireAndForget("socket/classroom-event", ticketEvent)
  }

  /**
   * Updates an existing ticket.
   * @param ticket The ticket to update.
   */
  public updateTicket(ticket: Ticket) {
    //this.http.put<Ticket[]>("/classroom-api/ticket", ticket).subscribe()
  }

  /**
   * Removes an existing ticket.
   * @param ticket The ticket to remove.
   */
  public removeTicket(ticket: Ticket) {
    //this.http.post<Ticket[]>("/classroom-api/ticket/delete", ticket).subscribe()
  }
}
