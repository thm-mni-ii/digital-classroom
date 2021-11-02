import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {ClassroomService} from "../../service/classroom.service";
import {InviteToConferenceDialogComponent} from "../invite-to-conference-dialog/invite-to-conference-dialog.component";
import {ConferenceInfo} from "../../model/ConferenceInfo";
import {Ticket} from "../../model/Ticket";

@Component({
  selector: 'app-link-conference-to-ticket-dialog',
  templateUrl: './link-conference-to-ticket-dialog.component.html',
  styleUrls: ['./link-conference-to-ticket-dialog.component.scss']
})
export class LinkConferenceToTicketDialogComponent implements OnInit {
  disabled: Boolean = false;

  constructor(@Inject(MAT_DIALOG_DATA) public data: LinkConferenceInputData,
              public dialogRef: MatDialogRef<InviteToConferenceDialogComponent>,
              public classroomService: ClassroomService
  ) { }

  ngOnInit(): void {
    this.dialogRef.afterOpened().subscribe(() => this.disabled = false);
  }

  public getConference(conference: ConferenceInfo) {
    this.close(conference.conferenceId)
  }

  public close(conferenceId?: string | null) {
    this.dialogRef.close(conferenceId)
  }

  public newConference() {
    this.classroomService.createNewConferenceForTicket(this.data.ticket).subscribe(
      conf => this.close(conf.conferenceId)
    )
  }

  public currentConferenceName(): string {
    if (this.data.ticket.conferenceId === null) return ""
    return this.data.conferences.find(conf => conf.conferenceId === this.data.ticket.conferenceId)!!.conferenceName
  }
}

export class LinkConferenceInputData {
  conferences: ConferenceInfo[]
  ticket: Ticket

  constructor(conferences: ConferenceInfo[], ticket: Ticket) {
    this.conferences = conferences;
    this.ticket = ticket;

  }

}
