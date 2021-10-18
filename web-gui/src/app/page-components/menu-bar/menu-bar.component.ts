import {Component, Input} from '@angular/core';
import {parseCourseRole, User} from 'src/app/model/User';
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
import {ClassroomInfo} from "../../model/ClassroomInfo";
import {UserIconService} from "../../util/user-icon.service";

@Component({
  selector: 'app-menu-bar',
  templateUrl: './menu-bar.component.html',
  styleUrls: ['./menu-bar.component.scss']
})
export class MenuBarComponent {

  public parseCourseRole: Function = parseCourseRole
  @Input() public currentUser: User | undefined
  @Input() public classroomInfo: ClassroomInfo | undefined
  menuVisible: boolean = false;

  constructor(
    public classroomService: ClassroomService,
    public conferenceService: ConferenceService,
    private authService: AuthService,
    private dialog: MatDialog,
    public userIconService: UserIconService
  ) {}

  public createConference() {
      this.dialog.open(CreateConferenceDialogComponent, {
        height: 'auto',
        width: 'auto',
        data: new CreateConferenceInputData(this.classroomService.classroomInfo!!, this.classroomService.currentUser!!)
      }).beforeClosed().pipe(
        filter(conferenceInfo => conferenceInfo instanceof ConferenceInfo),
      ).subscribe((conferenceInfo: ConferenceInfo) => {
        this.classroomService.createConference(conferenceInfo)
      });
  }

  toggleMenu() {
    this.menuVisible = !this.menuVisible;
  }

}
