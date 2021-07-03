import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {BbbConferenceHandlingService} from '../../service/bbb-conference-handling.service';
import {ClassroomService} from '../../service/classroom.service';
import {FormBuilder, FormGroup} from '@angular/forms';
import { first } from 'rxjs/operators';
import {User} from "../../model/User";

@Component({
  selector: 'app-inviteto-conference-dialog',
  templateUrl: './invite-to-conference-dialog.component.html',
  styleUrls: ['./invite-to-conference-dialog.component.scss']
})
export class InviteToConferenceDialogComponent implements OnInit {
  invitees: User[];
  form: FormGroup;
  disabled: Boolean = false;

  constructor(@Inject(MAT_DIALOG_DATA) public data: any,
              public dialogRef: MatDialogRef<InviteToConferenceDialogComponent>,
              private snackBar: MatSnackBar, private conferenceService: BbbConferenceHandlingService,
              private classroomService: ClassroomService, private _formBuilder: FormBuilder) {
  }

  ngOnInit(): void {
    this.invitees = this.data.users;
    this.dialogRef.afterOpened().subscribe(() => this.disabled = false);
  }

  public startCall(invitee) {
      if (this.disabled) {
        return;
      }
      this.disabled = true;
      this.classroomService.userInviter().pipe(first()).subscribe(() => {
        this.classroomService.inviteToConference(invitee);
      });
      this.classroomService.openConference();
      this.snackBar.open(`${invitee.prename} ${invitee.surname} wurde eingeladen der Konferenz beizutreten.`, 'OK', {duration: 3000});
      this.dialogRef.close();
  }

  public cancelCall() {
    this.dialogRef.close();
  }
}
