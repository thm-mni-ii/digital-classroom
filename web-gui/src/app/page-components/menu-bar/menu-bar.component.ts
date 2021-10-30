import { Component, Input, ViewChild } from '@angular/core';
import { parseCourseRole, User } from 'src/app/model/User';
import { ClassroomService } from '../../service/classroom.service';
import { ConferenceService } from '../../service/conference.service';
import { ClassroomInfo } from '../../model/ClassroomInfo';
import { UserIconService } from '../../util/user-icon.service';
import { DropdownMenuComponent } from './dropdown-menu/dropdown-menu.component';

@Component({
  selector: 'app-menu-bar',
  templateUrl: './menu-bar.component.html',
  styleUrls: ['./menu-bar.component.scss'],
})
export class MenuBarComponent {
  public parseCourseRole: Function = parseCourseRole;
  @Input() public currentUser: User | undefined;
  @Input() public classroomInfo: ClassroomInfo | undefined;

  constructor(
    public classroomService: ClassroomService,
    public conferenceService: ConferenceService,
    public userIconService: UserIconService
  ) {}
}
