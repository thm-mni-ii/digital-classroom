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

@Component({
  selector: 'app-menu-bar',
  templateUrl: './menu-bar.component.html',
  styleUrls: ['./menu-bar.component.scss']
})
export class MenuBarComponent {

  //colors: string[] = ['avatar-green', 'avatar-orange', 'avatar-blue', 'avatar-light-blue']
  colors: string[] = ['#FD9A63', '#60CB7E', '#26B8B8', '#405E9A']

  public parseCourseRole: Function = parseCourseRole
  @Input() public currentUser: User
  @Input() public classroomInfo: ClassroomInfo
  menuVisible: boolean = false;

  constructor(
    public classroomService: ClassroomService,
    public conferenceService: ConferenceService,
    private authService: AuthService,
    private dialog: MatDialog
  ) {}

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

  toggleMenu() {
    this.menuVisible = !this.menuVisible;
  }

  calculateColorClass(name: string): string {
   return this.colors[this.hashCode(name) % this.colors.length];
  }

  hashCode(str: string) {
    let hash = 0, i, chr;
    if (str.length === 0) return hash;
    for (i = 0; i < str.length; i++) {
      chr   = str.charCodeAt(i);
      hash  = ((hash << 5) - hash) + chr;
      hash |= 0; // Convert to 32bit integer
    }
    return hash;
  };

}
