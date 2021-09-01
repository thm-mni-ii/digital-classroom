import { Component } from '@angular/core';
import {defaultUserDisplay, parseCourseRole, UserDisplay} from 'src/app/model/User';
import {ClassroomService} from "../../service/classroom.service";
import {ConferenceService} from "../../service/conference.service";
import {AuthService} from "../../service/auth.service";


@Component({
  selector: 'app-menu-bar',
  templateUrl: './menu-bar.component.html',
  styleUrls: ['./menu-bar.component.scss']
})
export class MenuBarComponent {

  public parseCourseRole: Function = parseCourseRole
  public currentUser: UserDisplay = defaultUserDisplay(this.authService.getToken())

  constructor(
    public classroomService: ClassroomService,
    public conferenceService: ConferenceService,
    private authService: AuthService
) {
    classroomService.currentUserObservable.subscribe( userDisplay => {
      this.currentUser = userDisplay
    })
  }

}
