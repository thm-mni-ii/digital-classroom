import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {User} from "../model/User";

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(private http: HttpClient) {

  }

  /**
   * Gets all User in classroom.
   */
  public async getUsersInClassroom(): Promise<User[]> {
    return this.http.get<User[]>("/classroom-api/users").toPromise()
  }
}
