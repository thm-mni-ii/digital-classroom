import {Component, Input} from '@angular/core';
import {parseCourseRole, User, UserRole} from "../../../model/User";
import {ClassroomService} from "../../../service/classroom.service";

@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss']
})
export class UserListComponent {

  @Input() public users: User[]
  public parseCourseRole: Function = parseCourseRole

  constructor(
    public classroomService: ClassroomService
  ) {
  }

  public sortUsers(users: User[]) {
    return users
      .filter(user => user.visible)
      .sort(UserListComponent.compareUsers);
  }

  private static compareUsers(a: User, b: User) {
    if (UserListComponent.roleValue(a.userRole) > UserListComponent.roleValue(b.userRole)) {
      return 1;
    } else if (UserListComponent.roleValue(a.userRole) < UserListComponent.roleValue(b.userRole)) {
      return -1;
    } else {
      if (a.fullName > b.fullName) {
        return 1
      } else {
        return -1
      }
    }
  }

  private static roleValue(role: UserRole): number {
    if (role === UserRole.STUDENT) return 90
    if (role === UserRole.TUTOR) return 50
    if (role === UserRole.TEACHER) return 0
    else return 0
  }

}
