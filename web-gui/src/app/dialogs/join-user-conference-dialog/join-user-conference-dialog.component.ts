import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ClassroomService} from '../../service/classroom.service';
import {FormBuilder, FormGroup} from '@angular/forms';
import {UserDisplay} from "../../model/User";
import {ConferenceService} from "../../service/conference.service";

@Component({
  selector: 'app-invite-to-conference-dialog',
  templateUrl: './join-user-conference-dialog.component.html',
  styleUrls: ['./join-user-conference-dialog.component.scss']
})
export class JoinUserConferenceDialogComponent implements OnInit {
  form: FormGroup;
  disabled: Boolean = false;

  constructor(@Inject(MAT_DIALOG_DATA) public data: UserDisplay,
              public dialogRef: MatDialogRef<JoinUserConferenceDialogComponent>,
              private snackBar: MatSnackBar, private conferenceService: ConferenceService,
              public classroomService: ClassroomService, private _formBuilder: FormBuilder) {
  }

  ngOnInit(): void {
    this.dialogRef.afterOpened().subscribe(() => this.disabled = false);
  }

  public close() {
    this.dialogRef.close()
  }
}
