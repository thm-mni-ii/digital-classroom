import {Component, Inject} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Ticket} from '../../model/Ticket';
import {NotificationService} from "../../service/notification.service";
import {User} from "../../model/User";

@Component({
  selector: 'app-new-ticket-dialog',
  templateUrl: './new-ticket-dialog.component.html',
  styleUrls: ['./new-ticket-dialog.component.scss']
})
export class NewTicketDialogComponent {
  form: FormGroup;

  constructor(private _formBuilder: FormBuilder,
              @Inject(MAT_DIALOG_DATA) public currentUser: User,
              private notification: NotificationService,
              public dialogRef: MatDialogRef<NewTicketDialogComponent>
  ) {
    this.form = this._formBuilder.group({
      desc: ''
    });
  }

  createTicket() {
    const description = this.form.get('desc')!!.value.trim()
    const ticket = new Ticket(description, this.currentUser)
    if (ticket.description !== '') {
       this.notification.show(`Das Ticket wurde erfolgreich erstellt.`);
       this.dialogRef.close(ticket);
     } else {
       this.notification.show(`Das Ticket konnte nicht erstellt werden!`);
     }
  }

  close() {
    this.dialogRef.close();
  }
}
