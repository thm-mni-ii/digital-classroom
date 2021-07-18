import {Pipe, PipeTransform} from '@angular/core';
import {Roles} from "../model/Roles";

@Pipe({
  name: 'isPrivileged',
  pure: false
})
export class IsPrivilegedPipe implements PipeTransform {
  transform(items: any[]): any {
    if (!items) {
      return items;
    }
    return items.filter(item => Roles.isPrivileged(item.userRole));
  }
}
