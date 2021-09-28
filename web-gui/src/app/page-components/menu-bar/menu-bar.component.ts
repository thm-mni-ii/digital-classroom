import {Component, Input} from '@angular/core';
import {parseCourseRole, UserDisplay} from 'src/app/model/User';
import {ClassroomService} from "../../service/classroom.service";
import {ConferenceService} from "../../service/conference.service";
import {AuthService} from "../../service/auth.service";
import {MatDialog} from "@angular/material/dialog";
import {filter} from "rxjs/operators";
import {
  CreateConferenceDialogComponent,
  CreateConferenceInputData
} from "../../dialogs/create-conference-dialog/create-conference-dialog.component";
import {ConferenceInfo} from "../../model/ConferenceInfo";


@Component({
  selector: 'app-menu-bar',
  templateUrl: './menu-bar.component.html',
  styleUrls: ['./menu-bar.component.scss']
})
export class MenuBarComponent {

  public parseCourseRole: Function = parseCourseRole
  @Input() public currentUser: UserDisplay

  constructor(
    public classroomService: ClassroomService,
    public conferenceService: ConferenceService,
    private authService: AuthService,
    private dialog: MatDialog
  ) {

  }

  public createConference() {
      this.dialog.open(CreateConferenceDialogComponent, {
        height: 'auto',
        width: 'auto',
        data: new CreateConferenceInputData(this.classroomService.classroomInfo, this.classroomService.currentUser)
      }).beforeClosed().pipe(
        filter(conferenceInfo => conferenceInfo instanceof ConferenceInfo),
      ).subscribe((conferenceInfo: ConferenceInfo) => {
        this.classroomService.createConference(conferenceInfo)
      });
  }

}
