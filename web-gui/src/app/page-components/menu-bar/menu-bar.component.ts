import { Component } from '@angular/core';
import { parseCourseRole } from 'src/app/model/User';
import {ClassroomService} from "../../service/classroom.service";
import {ConferenceService} from "../../service/conference.service";


@Component({
  selector: 'app-menu-bar',
  templateUrl: './menu-bar.component.html',
  styleUrls: ['./menu-bar.component.scss']
})
export class MenuBarComponent {

  public parseCourseRole: Function = parseCourseRole

  constructor(
    public classroomService: ClassroomService,
    public conferenceService: ConferenceService
  ) {
  }

}
