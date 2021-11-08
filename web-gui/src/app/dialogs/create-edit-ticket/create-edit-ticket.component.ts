import {Component, Inject} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Ticket} from '../../model/Ticket';
import {NotificationService} from "../../service/notification.service";
import {User} from "../../model/User";
import {ConferenceInfo} from "../../model/ConferenceInfo";

@Component({
  selector: 'app-create-edit-ticket-dialog',
  templateUrl: './create-edit-ticket.component.html',
  styleUrls: ['./create-edit-ticket.component.scss']
})
export class CreateEditTicketComponent {
  form: FormGroup;
  title: string = "Neues Ticket erstellen"
  ticket: Ticket
  conferences: ConferenceInfo[]

  constructor(private _formBuilder: FormBuilder,
              @Inject(MAT_DIALOG_DATA) public ticketData: TicketEditData,
              private notification: NotificationService,
              public dialogRef: MatDialogRef<CreateEditTicketComponent>
  ) {
    this.conferences = this.ticketData.conferences
    if (ticketData.ticket !== undefined) {
      this.title = "Ticket bearbeiten"
      this.ticket = ticketData.ticket
    } else {
      this.ticket = new Ticket("", ticketData.currentUser)
      this.ticket.conferenceId = this.conferences.length > 0
        ? this.conferences.reduce((c1, c2) => c1.creationTimestamp > c2.creationTimestamp ? c1 : c2).conferenceId
        : null
    }
    this.form = this._formBuilder.group({
      desc: this.ticket.description,
      conf: this.ticket.conferenceId
    });
  }

  editTicket() {
    const description = this.form.get('desc')!!.value.trim()
    const conferenceId = this.form.get('conf')!!.value
    if (description === this.ticket.description) close()
    this.ticket.description = description
    this.ticket.conferenceId = conferenceId
    if (this.ticket.description !== '') {
       this.notification.show(`Das Ticket wurde erfolgreich erstellt.`);
       this.dialogRef.close(this.ticket);
     } else {
       this.notification.show(`Das Ticket konnte nicht erstellt werden!`);
     }
  }

  close() {
    this.dialogRef.close();
  }
}

export class TicketEditData {

  currentUser: User
  ticket?: Ticket
  conferences: ConferenceInfo[] = []

  constructor(currentUser: User, conferences: ConferenceInfo[], ticket?: Ticket) {
    this.currentUser = currentUser
    this.conferences = conferences
    this.ticket = ticket
  }

}
