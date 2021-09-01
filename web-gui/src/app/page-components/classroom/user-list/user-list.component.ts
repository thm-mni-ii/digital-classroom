import {Component, Input} from '@angular/core';
import {User, UserDisplay} from "../../../model/User";
import {ClassroomService} from "../../../service/classroom.service";
import {parseCourseRole} from "../../../model/User";

@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['../classroom.component.scss', './user-list.component.scss']
})
export class UserListComponent {

  @Input() public users: UserDisplay[]
  public parseCourseRole: Function = parseCourseRole

  constructor(
    public classroomService: ClassroomService
  ) { }

  inviteToConference(user: User) {

  }



}
