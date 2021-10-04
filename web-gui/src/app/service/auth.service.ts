import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders, HttpResponse} from '@angular/common/http';
import {JwtHelperService} from '@auth0/angular-jwt';
import {Observable, timer} from 'rxjs';
import {map, tap} from 'rxjs/operators';
import {Params} from "@angular/router";
import {User} from "../model/User";

const JWT_STORAGE = 'classroom-token';
const REFRESH_STORAGE = 'classroom-refresh-token';

/**
 * Manages login and logout of the user of the page.
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {

  constructor(private http: HttpClient, private jwtHelper: JwtHelperService) {
    this.startTokenAutoRefresh()
  }

  private static extractJwtFromHeader(response: HttpResponse<any>): string {
    const authHeader: string = response.headers.get('Authorization');
    return authHeader ? authHeader.replace('Bearer ', '') : null;
  }

  private static extractRefreshTokenFromHeader(response: HttpResponse<any>): string {
    const refreshToken: string = response.headers.get('refresh_token');
    return refreshToken ? refreshToken : null;
  }

  /**
   * Returns true only if a valid token exists.
   */
  public isAuthenticated(): boolean {
    const token = this.loadToken();
    return token && !this.jwtHelper.isTokenExpired(token);
  }

  /**
   * @return The lastly received token.
   */
  public getToken(): User {
    const token = this.loadToken();
    const decodedToken = this.decodeToken(token);
    if (!decodedToken) {
      throw new Error('Decoding the token failed');
    } else if (this.jwtHelper.isTokenExpired(token)) {
      throw new Error('Token expired');
    }
    return decodedToken;
  }

  private decodeToken(token: string): User {
    return this.jwtHelper.decodeToken<User>(token);
  }

  /**
   * @return Get token as string or null if no token exists.
   */
  public loadToken(): string {
    return localStorage.getItem(JWT_STORAGE);
  }

  public loadRefreshToken(): string {
    return localStorage.getItem(REFRESH_STORAGE);
  }

  private static storeToken(token: string): void {
    localStorage.setItem(JWT_STORAGE, token);
  }

  private storeRefreshToken(token: string): void {
    localStorage.setItem(REFRESH_STORAGE, token);
    this.startTokenAutoRefresh();
  }

  public requestNewToken() {
    let headers = new HttpHeaders().set('refresh_token', this.loadRefreshToken())
    return this.http.get<void>('/classroom-api/refresh', {headers: headers, observe: 'response'})
      .pipe(
        tap(res => AuthService.storeToken(AuthService.extractJwtFromHeader(res))),
        tap(res => this.storeRefreshToken(AuthService.extractRefreshTokenFromHeader(res))),
      ).subscribe();
  }

  public useSessionToken(params: Params): Observable<User> {
    return this.http.get<void>('/classroom-api/join',
      {params: params, observe: 'response'})
      .pipe(
        tap(res => {
          const token = AuthService.extractJwtFromHeader(res);
          const refreshToken = AuthService.extractRefreshTokenFromHeader(res)
          AuthService.storeToken(token);
          this.storeRefreshToken(refreshToken)
        }),
        map(() => this.getToken()),
      );
  }

  /**
   * Logout user by removing its token.
   */
  public logout() {
    localStorage.removeItem(JWT_STORAGE);
    localStorage.removeItem(REFRESH_STORAGE);
  }

  private startTokenAutoRefresh() {
      if (this.isAuthenticated()) {
        const token: string = this.loadToken();
        const now: Date = new Date()
        const expDate: Date = this.jwtHelper.getTokenExpirationDate(token)
        if (expDate === null) {
          console.warn("JWT has no expiration Date!")
          return
        }
        const reqDate = new Date(expDate.getTime() - 5000)

        if (now >= reqDate) {
          this.requestNewToken()
        } else {
          console.log("Token expires at " + expDate + "!")
          console.log("Request at " + reqDate + "!")

          timer(reqDate).pipe(
            tap(() => this.requestNewToken()),
            tap(test => console.log(test))
          ).subscribe()
        }
      }
  }
}
