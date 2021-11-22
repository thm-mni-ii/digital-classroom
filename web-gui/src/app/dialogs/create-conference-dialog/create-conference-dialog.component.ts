import {Component, Inject} from '@angular/core';
import {FormBuilder, FormControl, FormGroup} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {NotificationService} from "../../service/notification.service";
import {User} from "../../model/User";
import {ConferenceInfo} from "../../model/ConferenceInfo";
import {ClassroomInfo} from "../../model/ClassroomInfo";

@Component({
  selector: 'app-create-conference-dialog',
  templateUrl: './create-conference-dialog.component.html',
  styleUrls: ['./create-conference-dialog.component.scss']
})
export class CreateConferenceDialogComponent {
  form: FormGroup;
  conferenceSubject: FormControl
  conferenceVisible: FormControl
  title: string = "Neue Konferenz erstellen"

  constructor(private fb: FormBuilder,
              @Inject(MAT_DIALOG_DATA) public data: CreateConferenceInputData,
              private notification: NotificationService,
              private dialogRef: MatDialogRef<CreateConferenceDialogComponent>
  ) {
    if (this.data.plenary) {
      this.conferenceSubject = new FormControl('Plenum: ' + this.data.classroom.classroomName)
      this.title = "Plenarkonferenz erstellen"
    } else {
      this.conferenceSubject = new FormControl('Meeting von ' + this.data.currentUser.fullName)
    }
    this.conferenceVisible = new FormControl(true)

    this.form = this.fb.group({
      conferenceSubject: this.conferenceSubject,
      conferenceVisible: this.conferenceVisible,
    });
  }

  createConference() {
    const newConferenceInfo = new ConferenceInfo()
    newConferenceInfo.creator = this.data.currentUser
    newConferenceInfo.creationTimestamp = Date.now()
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
  currentUser: User
  plenary: boolean

  constructor(classroom: ClassroomInfo, currentUser: User, plenary: boolean = false) {
    this.currentUser = currentUser;
    this.classroom = classroom;
    this.plenary = plenary
  }
}

