import { Pipe, PipeTransform } from '@angular/core';
import {UserCredentials} from "../model/User";

@Pipe({
  name: 'isNotSelf'
})
export class IsNotSelfPipe implements PipeTransform {

  transform(items: UserCredentials[], self: UserCredentials): UserCredentials[] {
    if (!items) {
      return items;
    }
    return items.filter(item => item.userId != self.userId);
  }

}
