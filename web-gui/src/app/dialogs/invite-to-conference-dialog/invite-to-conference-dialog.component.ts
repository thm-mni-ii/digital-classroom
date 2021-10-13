import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {ClassroomService} from '../../service/classroom.service';
import {User} from "../../model/User";

@Component({
  selector: 'app-invite-to-conference-dialog',
  templateUrl: './invite-to-conference-dialog.component.html',
  styleUrls: ['./invite-to-conference-dialog.component.scss']
})
export class InviteToConferenceDialogComponent implements OnInit {
  disabled: Boolean = false;

  constructor(@Inject(MAT_DIALOG_DATA) public data: User,
              public dialogRef: MatDialogRef<InviteToConferenceDialogComponent>,
              public classroomService: ClassroomService
  ) { }

  ngOnInit(): void {
    this.dialogRef.afterOpened().subscribe(() => this.disabled = false);
  }

  public close() {
    this.dialogRef.close()
  }
}
