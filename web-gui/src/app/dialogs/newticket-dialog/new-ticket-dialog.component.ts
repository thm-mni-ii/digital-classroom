import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {ClassroomService} from '../../service/classroom.service';
import {UserService} from '../../service/user.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSliderModule} from '@angular/material/slider';
import {Ticket} from '../../model/Ticket';
import {User} from "../../model/User";

@Component({
  selector: 'app-newticket-dialog',
  templateUrl: './new-ticket-dialog.component.html',
  styleUrls: ['./new-ticket-dialog.component.scss']
})
export class NewTicketDialogComponent implements OnInit {
  form: FormGroup | undefined;
  priority: number | undefined;

  constructor(private _formBuilder: FormBuilder, @Inject(MAT_DIALOG_DATA) public data: any,
              private snackBar: MatSnackBar, public dialogRef: MatDialogRef<NewTicketDialogComponent>,
              private classroomService: ClassroomService, private userService: UserService,
              private matSliderModule: MatSliderModule) { }

  ngOnInit(): void {
    this.form = this._formBuilder.group({
      desc: '',
      priority: 1,
    });
  }

  createTicket() {
     const ticket: Ticket = {
       id: undefined,
       desc: this.form!.get('desc')!.value.trim(),
       priority: this.form!.get('priority')!.value,
       courseId: undefined,
       timestamp: Date.now(),
       status: 'open',
       creator: undefined,
       assignee: undefined,
     };
     if (ticket.desc !== '' && ticket!.priority! > 0 && ticket!.priority! <= 10) {
       this.classroomService.createTicket(ticket);
       this.snackBar.open(`Das Ticket wurde erfolgreich erstellt.`, 'OK', {duration: 3000});
       this.dialogRef.close();
     } else {
       this.snackBar.open(`Das Ticket konnte nicht erstellt werden!`, 'OK', {duration: 3000});
     }
  }

  close() {
    this.dialogRef.close();
  }
}
