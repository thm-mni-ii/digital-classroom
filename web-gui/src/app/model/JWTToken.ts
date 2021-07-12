/**
 * The decoded jwt token of a successfully authenticated user.
 */
import {User} from "./User";

export interface JWTToken extends User {
  exp: number;
}
