import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ClassroomService} from '../../service/classroom.service';
import {FormBuilder, FormGroup} from '@angular/forms';
import {User} from "../../model/User";
import {ConferenceService} from "../../service/conference.service";

@Component({
  selector: 'app-inviteto-conference-dialog',
  templateUrl: './invite-to-conference-dialog.component.html',
  styleUrls: ['./invite-to-conference-dialog.component.scss']
})
export class InviteToConferenceDialogComponent implements OnInit {
  form: FormGroup;
  disabled: Boolean = false;

  constructor(@Inject(MAT_DIALOG_DATA) public data: User,
              public dialogRef: MatDialogRef<InviteToConferenceDialogComponent>,
              private snackBar: MatSnackBar, private conferenceService: ConferenceService,
              private classroomService: ClassroomService, private _formBuilder: FormBuilder) {
  }

  ngOnInit(): void {
    this.dialogRef.afterOpened().subscribe(() => this.disabled = false);
  }

  public startCall(invitee: User) {
      if (this.disabled) {
        return;
      }
      this.disabled = true;
      this.classroomService.inviteToConference(invitee);
      this.snackBar.open(`${invitee.fullName} wurde eingeladen der Konferenz beizutreten.`, 'OK', {duration: 3000});
      this.dialogRef.close();
  }

  public cancelCall() {
    this.dialogRef.close();
  }
}
