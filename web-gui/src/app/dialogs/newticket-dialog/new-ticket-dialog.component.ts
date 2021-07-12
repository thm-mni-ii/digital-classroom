import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Ticket} from '../../model/Ticket';

@Component({
  selector: 'app-newticket-dialog',
  templateUrl: './new-ticket-dialog.component.html',
  styleUrls: ['./new-ticket-dialog.component.scss']
})
export class NewTicketDialogComponent implements OnInit {
  form: FormGroup;
  ticket: Ticket = {
    description: "",
    createTime: Date.now(),
    status: 'open',
    creator: null,
    assignee: null,
  };



  constructor(private _formBuilder: FormBuilder, @Inject(MAT_DIALOG_DATA) public data: any,
              private snackBar: MatSnackBar, public dialogRef: MatDialogRef<NewTicketDialogComponent>) { }

  ngOnInit(): void {
    this.form = this._formBuilder.group({
      desc: '',
      priority: 1,
    });
  }

  createTicket() {
    this.ticket.description = this.form.get('desc').value.trim()
     if (this.ticket.description !== '') {
       this.snackBar.open(`Das Ticket wurde erfolgreich erstellt.`, 'OK', {duration: 3000});
       this.dialogRef.close(this.ticket);
     } else {
       this.snackBar.open(`Das Ticket konnte nicht erstellt werden!`, 'OK', {duration: 3000});
     }
  }

  close() {
    this.dialogRef.close();
  }
}
