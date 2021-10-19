import {Component, Input} from '@angular/core';
import {parseCourseRole, User} from "../../../../model/User";
import {ClassroomService} from "../../../../service/classroom.service";
import {filter, tap} from "rxjs/operators";
import {UserIconService} from "../../../../util/user-icon.service";

@Component({
  selector: 'app-user-list-entry',
  templateUrl: './user-list-entry.component.html',
  styleUrls: ['./user-list-entry.component.scss']
})
export class UserListEntryComponent {

  public parseCourseRole: Function = parseCourseRole

  @Input() user?: User

  constructor(
    public classroomService: ClassroomService,
    public   userIconService: UserIconService
  ) {

  }

  public joinConferenceOfUser(user: User) {
    this.classroomService.chooseConferenceOfUser(user).pipe(
      filter(conf => conf !== undefined),
      tap(conf => this.classroomService.conferenceService.joinConference(conf))
    ).subscribe()
  }

}
