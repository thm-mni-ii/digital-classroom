import { Pipe, PipeTransform } from '@angular/core';
import {User} from "../model/User";

@Pipe({
  name: 'isNotSelf'
})
export class IsNotSelfPipe implements PipeTransform {

  transform(items: User[], self: User): User[] {
    if (!items) {
      return items;
    }
    return items.filter(item => item.userId != self.userId);
  }

}
