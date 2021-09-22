import {Component, Input} from '@angular/core';
import {UserDisplay} from "../../../model/User";
import {ClassroomService} from "../../../service/classroom.service";
import {parseCourseRole} from "../../../model/User";

@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss']
})
export class UserListComponent {

  @Input() public users: UserDisplay[]
  public parseCourseRole: Function = parseCourseRole

  constructor(
    public classroomService: ClassroomService
  ) { }

  public sortUsers(users: UserDisplay[]) {
    return users.sort((a, b) => {
      if (a.userRole > b.userRole) {
        return 1;
      } else if ( a.userRole < b.userRole) {
        return -1;
      } else {
        if (a.userRole > b.userRole) {
          return 1;
        } else {
          return -1;
        }
      }
    });
  }

}
