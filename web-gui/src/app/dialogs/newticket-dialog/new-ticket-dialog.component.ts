import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Ticket} from '../../model/Ticket';
import {AuthService} from "../../service/auth.service";

@Component({
  selector: 'app-newticket-dialog',
  templateUrl: './new-ticket-dialog.component.html',
  styleUrls: ['./new-ticket-dialog.component.scss']
})
export class NewTicketDialogComponent implements OnInit {
  form: FormGroup;

  constructor(private _formBuilder: FormBuilder,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private snackBar: MatSnackBar,
              public dialogRef: MatDialogRef<NewTicketDialogComponent>
  ) { }

  ngOnInit(): void {
    this.form = this._formBuilder.group({
      desc: '',
      priority: 1,
    });
  }

  createTicket() {
    const description = this.form.get('desc').value.trim()
    const ticket = new Ticket(description)
    if (ticket.description !== '') {
       this.snackBar.open(`Das Ticket wurde erfolgreich erstellt.`, 'OK', {duration: 3000});
       this.dialogRef.close(ticket);
     } else {
       this.snackBar.open(`Das Ticket konnte nicht erstellt werden!`, 'OK', {duration: 3000});
     }
  }

  close() {
    this.dialogRef.close();
  }
}
