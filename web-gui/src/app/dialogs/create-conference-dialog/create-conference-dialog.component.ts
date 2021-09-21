import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, FormControl, FormGroup} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {NotificationService} from "../../service/notification.service";
import {UserDisplay} from "../../model/User";
import {ConferenceInfo} from "../../model/ConferenceInfo";
import {ClassroomInfo} from "../../model/ClassroomInfo";

@Component({
  selector: 'app-create-conference-dialog',
  templateUrl: './create-conference-dialog.component.html',
  styleUrls: ['./create-conference-dialog.component.scss']
})
export class CreateConferenceDialogComponent implements OnInit {
  form: FormGroup;
  conferenceSubject: FormControl
  conferenceVisible: FormControl

  constructor(private fb: FormBuilder,
              @Inject(MAT_DIALOG_DATA) public data: CreateConferenceInputData,
              private notification: NotificationService,
              private dialogRef: MatDialogRef<CreateConferenceDialogComponent>) {}

  ngOnInit(): void {
    this.conferenceSubject = new FormControl('Konferenz von ' + this.data.currentUser.fullName)
    this.conferenceVisible = new FormControl(true)

    this.form = this.fb.group({
      conferenceSubject: this.conferenceSubject,
      conferenceVisible: this.conferenceVisible,
    });
  }

  createConference() {
    const newConferenceInfo = new ConferenceInfo()
    newConferenceInfo.creator = this.data.currentUser
    newConferenceInfo.creation = Date.now()
    newConferenceInfo.classroomId = this.data.classroom.classroomId
    newConferenceInfo.conferenceName = this.conferenceSubject.value
    newConferenceInfo.visible = this.conferenceVisible.value
    this.dialogRef.close(newConferenceInfo);
  }

  close() {
    this.dialogRef.close();
  }
}

export class CreateConferenceInputData {
  classroom: ClassroomInfo
  currentUser: UserDisplay

  constructor(classroom: ClassroomInfo, currentUser: UserDisplay) {
    this.currentUser = currentUser;
    this.classroom = classroom;
  }
}

