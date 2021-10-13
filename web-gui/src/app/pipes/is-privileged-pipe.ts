import {Pipe, PipeTransform} from '@angular/core';
import {Roles} from "../model/Roles";
import {UserCredentials} from "../model/User";

@Pipe({
  name: 'isPrivileged',
  pure: false
})
export class IsPrivilegedPipe implements PipeTransform {
  transform(users: UserCredentials[]): UserCredentials[] {
    if (!users) {
      return users;
    }
    return users.filter(user => Roles.isPrivileged(user.userRole));
  }
}
