/**
 * The decoded jwt token of a successfully authenticated user.
 */
import {User} from "./User";
import {Roles} from "./Roles";

export class JWTToken extends User {
  exp: number;

  isPrivileged(): boolean {
    return Roles.isTeacher(this.userRole) || Roles.isTutor(this.userRole)
  }
}
