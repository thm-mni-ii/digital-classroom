import {Injectable} from '@angular/core';
import {Ticket} from "../model/Ticket";
import {RSocketService} from "../rsocket/r-socket.service";
import {BehaviorSubject, Observable, Subject} from "rxjs";
import {TicketAction, TicketEvent} from "../rsocket/event/TicketEvent";
import {EventListenerService} from "../rsocket/event-listener.service";
import {finalize, map, tap} from "rxjs/operators";
import {ConferenceInfo} from "../model/ConferenceInfo";

@Injectable({
  providedIn: 'root'
})
export class TicketService {

  private tickets: Ticket[] = []
  // @ts-ignore
  private ticketSubject: Subject<Ticket[]> = new BehaviorSubject([])
  ticketObservable: Observable<Ticket[]> = this.ticketSubject.asObservable()

  private newTicketSubject: Subject<Ticket> = new Subject<Ticket>()
  newTicketObservable: Observable<Ticket> = this.newTicketSubject.asObservable()

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
      tap(ticket => this.tickets.push(ticket)),
      finalize(() => this.publish())
    ).subscribe()
  }

  private handleTicketEvent(ticketEvent: TicketEvent) {
    switch (ticketEvent.ticketAction) {
      case TicketAction.CREATE: {
        this.tickets.push(ticketEvent.ticket)
        this.newTicketSubject.next(ticketEvent.ticket)
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
    const ticketEvent = new TicketEvent(ticket, TicketAction.CREATE)
    return this.rSocketService.fireAndForget("socket/classroom-event", ticketEvent)
  }

  /**
   * Assigns a ticket.
   * @param ticket The ticket to assign.
   */
  public assignTicket(ticket: Ticket) {
    const ticketEvent = new TicketEvent(ticket, TicketAction.ASSIGN)
    return this.rSocketService.fireAndForget("socket/classroom-event", ticketEvent)
  }

  /**
   * Edits (changes conference or description) a ticket.
   * @param ticket The ticket to edit.
   */
  public editTicket(ticket: Ticket) {
    const ticketEvent = new TicketEvent(ticket, TicketAction.EDIT)
    return this.rSocketService.fireAndForget("socket/classroom-event", ticketEvent)
  }

  /**
   * Removes an existing ticket.
   * @param ticket The ticket to remove.
   */
  public removeTicket(ticket: Ticket) {
    const ticketEvent = new TicketEvent(ticket, TicketAction.CLOSE)
    return this.rSocketService.fireAndForget("socket/classroom-event", ticketEvent)
  }

  public getTicketOfConference(conferenceInfo: ConferenceInfo): Ticket | null {
    const ticket = this.tickets.find(ticket => ticket.conferenceId === conferenceInfo.conferenceId)
    if (ticket === undefined) return null
    else return ticket
  }
}
