import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ClassroomService} from '../../service/classroom.service';
import {FormBuilder, FormGroup} from '@angular/forms';
import {UserDisplay} from "../../model/User";
import {ConferenceService} from "../../service/conference.service";
import {ConferenceInfo} from "../../model/ConferenceInfo";

@Component({
  selector: 'app-invite-to-conference-dialog',
  templateUrl: './invite-to-conference-dialog.component.html',
  styleUrls: ['./invite-to-conference-dialog.component.scss']
})
export class InviteToConferenceDialogComponent implements OnInit {
  form: FormGroup;
  disabled: Boolean = false;

  constructor(@Inject(MAT_DIALOG_DATA) public data: UserDisplay,
              public dialogRef: MatDialogRef<InviteToConferenceDialogComponent>,
              private snackBar: MatSnackBar, private conferenceService: ConferenceService,
              public classroomService: ClassroomService, private _formBuilder: FormBuilder) {
  }

  ngOnInit(): void {
    this.dialogRef.afterOpened().subscribe(() => this.disabled = false);
  }

  public selectConference(conference: ConferenceInfo) {
    this.dialogRef.close(conference);
  }

  public close() {
    this.dialogRef.close()
  }
}
