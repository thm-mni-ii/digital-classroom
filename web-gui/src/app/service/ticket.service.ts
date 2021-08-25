import { Injectable } from '@angular/core';
import {Ticket} from "../model/Ticket";
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class TicketService {

  constructor(private http: HttpClient) {

  }

  public getTickets(): Promise<Ticket[]> {
    return this.http.get<Ticket[]>("/classroom-api/ticket").toPromise()
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
    this.http.post<Ticket[]>("/classroom-api/ticket/delete", ticket).subscribe()
  }
}
