import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams, HttpResponse} from '@angular/common/http';
import {JwtHelperService} from '@auth0/angular-jwt';
import {Observable, timer} from 'rxjs';
import {tap} from 'rxjs/operators';
import {UserCredentials} from "../model/User";

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
    if (this.isAuthenticated()) {
      this.startTokenAutoRefresh()
    }
  }

  private static extractJwtFromHeader(response: HttpResponse<any>): string {
    if (!response.headers.has('Authorization')) {
      throw Error("No Authorization token in HttpResponse!")
    }
    const authHeader: string = response.headers.get('Authorization')!!;
    return authHeader.replace('Bearer ', '');
  }

  private static extractRefreshTokenFromHeader(response: HttpResponse<any>): string {
    if (!response.headers.has('refreshToken')) {
      throw Error("No refreshToken token in HttpResponse!")
    }
    return response.headers.get('refreshToken')!!;
  }

  /**
   * Returns true only if a valid token exists.
   */
  public isAuthenticated(): boolean {
    let token = null
    try {
      token = this.loadToken();
    } catch (error) {
      return false;
    }
    return token !== null && !this.jwtHelper.isTokenExpired(token);
  }

  /**
   * @return The lastly received token.
   */
  public getToken(): UserCredentials {
    const token = this.loadToken();
    const decodedToken = this.decodeToken(token);
    if (!decodedToken) {
      throw new Error('Decoding the token failed');
    } else if (this.jwtHelper.isTokenExpired(token)) {
      throw new Error('Token expired');
    }
    return decodedToken;
  }

  private decodeToken(token: string): UserCredentials {
    return this.jwtHelper.decodeToken<UserCredentials>(token);
  }

  /**
   * @return Get token as string or null if no token exists.
   */
  public loadToken(): string {
    const token = localStorage.getItem(JWT_STORAGE);
    if (token === null) return ""
    return token
  }

  public loadRefreshToken(): string {
    const token = localStorage.getItem(REFRESH_STORAGE)
    if (token === null) throw new Error('No refresh token stored!');
    return token;
  }

  private static storeToken(token: string): void {
    localStorage.setItem(JWT_STORAGE, token);
  }

  private storeRefreshToken(token: string): void {
    localStorage.setItem(REFRESH_STORAGE, token);
    this.startTokenAutoRefresh();
  }

  public requestNewToken() {
    const headers = new HttpHeaders().set('refreshToken', this.loadRefreshToken())
    return this.http.get<void>('/classroom-api/refresh', {headers: headers, observe: 'response'})
      .pipe(
        tap(res => AuthService.storeToken(AuthService.extractJwtFromHeader(res))),
        tap(res => this.storeRefreshToken(AuthService.extractRefreshTokenFromHeader(res))),
      ).subscribe();
  }

  public useSessionToken(sessionToken: string): Observable<HttpResponse<void>> {
    const params = new HttpParams().set("sessionToken", sessionToken)
    return this.http.get<void>('/classroom-api/join',
      {params: params, observe: 'response'})
      .pipe(
        tap(res => {
          const token = AuthService.extractJwtFromHeader(res);
          const refreshToken = AuthService.extractRefreshTokenFromHeader(res)
          AuthService.storeToken(token);
          this.storeRefreshToken(refreshToken)
        })
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
        const expDate: Date | null = this.jwtHelper.getTokenExpirationDate(token)
        if (expDate === null) {
          console.warn("JWT has no expiration Date!")
          return
        }
        const reqDate = new Date(expDate.getTime() - 5000)

        if (now >= reqDate) {
          this.requestNewToken()
        } else {
          timer(reqDate).pipe(
            tap(() => this.requestNewToken()),
            tap(test => console.log(test))
          ).subscribe()
        }
      }
  }
}
