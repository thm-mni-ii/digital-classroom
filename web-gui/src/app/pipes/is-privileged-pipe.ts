import {Pipe, PipeTransform} from '@angular/core';
import {Roles} from "../model/Roles";
import {User} from "../model/User";

@Pipe({
  name: 'isPrivileged',
  pure: false
})
export class IsPrivilegedPipe implements PipeTransform {
  transform(users: User[]): User[] {
    if (!users) {
      return users;
    }
    return users.filter(user => user.isAuthorized());
  }
}
