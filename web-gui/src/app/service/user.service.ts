import { Injectable } from '@angular/core';
import {User} from "../model/User";
import {RSocketService} from "../rsocket/r-socket.service";
import {Observable} from "rxjs";
import {UserEvent} from "../rsocket/event/ClassroomEvent";

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(
    private rSocketService: RSocketService) {

  }

  /**
   * Gets all User in classroom.
   */
  public getUsersInClassroom(): Observable<UserEvent> {
    return this.rSocketService.requestStream<UserEvent>("socket/init-users")
  }
}
