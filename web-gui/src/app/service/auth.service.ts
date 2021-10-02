import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders, HttpResponse} from '@angular/common/http';
import {JwtHelperService} from '@auth0/angular-jwt';
import {Observable, of, throwError} from 'rxjs';
import {map, mergeMap, tap} from 'rxjs/operators';
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

  /**
   * Renews token taken from the http response.
   * @param response The http response.
   */
  public renewToken(response: HttpResponse<any>) {
    const token = AuthService.extractJwtFromHeader(response);
    if (token && !this.jwtHelper.isTokenExpired(token)) {
      AuthService.storeToken(token);
    }
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

  private static storeRefreshToken(token: string): void {
    localStorage.setItem(REFRESH_STORAGE, token);
  }

  public requestNewToken() {
    let headers = new HttpHeaders().set('refresh_token', this.loadRefreshToken())
    return this.http.get<void>('/classroom-api/refresh', {headers: headers, observe: 'response'})
      .pipe(
        tap(res => AuthService.storeToken(AuthService.extractJwtFromHeader(res))),
        tap(res => AuthService.storeRefreshToken(AuthService.extractRefreshTokenFromHeader(res)))
      ).subscribe();
  }

  public useSessionToken(params: Params): Observable<User> {
    return this.http.get<void>('/classroom-api/join',
      {params: params, observe: 'response'})
      .pipe(map(res => {
        const token = AuthService.extractJwtFromHeader(res);
        const refreshToken = AuthService.extractRefreshTokenFromHeader(res)
        AuthService.storeToken(token);
        AuthService.storeRefreshToken(refreshToken)
        return token;
      }), mergeMap(token => {
        const decodedToken = this.decodeToken(token);
        if (!decodedToken) {
          return throwError('Decoding the token failed');
        } else if (this.jwtHelper.isTokenExpired(token)) {
          return throwError('Token expired');
        }
        return of(decodedToken)
      }));
  }

  /**
   * Logout user by removing its token.
   */
  public logout() {
    localStorage.removeItem(JWT_STORAGE);
    localStorage.removeItem(REFRESH_STORAGE);
  }

  private startTokenAutoRefresh() {
    setInterval(() => {
      if (this.isAuthenticated()) {
        const token = this.loadToken();
        if (this.jwtHelper.isTokenExpired(token, 70)) {
          this.requestNewToken();
        }
      }
    }, 60000);
  }
}
