import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {JwtHelperService} from '@auth0/angular-jwt';
import {Observable} from 'rxjs';
import {of, throwError} from 'rxjs';
import {mergeMap, map} from 'rxjs/operators';
import {JWTToken} from '../model/JWTToken';
import {Params} from "@angular/router";

const TOKEN_ID = 'token';

/**
 * Manages login and logout of the user of the page.
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {

  constructor(private http: HttpClient, private jwtHelper: JwtHelperService) {}

  /**
   * Logout user by removing its token.
   */
  public logout() {
    localStorage.removeItem(TOKEN_ID);
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
  getToken(): JWTToken {
    const token = this.loadToken();
    const decodedToken = this.decodeToken(token);
    if (!decodedToken) {
      throw new Error('Decoding the token failed');
    } else if (this.jwtHelper.isTokenExpired(token)) {
      throw new Error('Token expired');
    }
    decodedToken.role = JSON.parse(<any>decodedToken.role);
    return decodedToken;
  }

  /**
   * Renews token taken from the http response.
   * @param response The http response.
   */
  public renewToken(response: HttpResponse<any>) {
    const token = this.extractTokenFromHeader(response);
    if (token && !this.jwtHelper.isTokenExpired(token)) {
      this.storeToken(token);
    }
  }

  private decodeToken(token: string): JWTToken | null {
    return this.jwtHelper.decodeToken(localStorage.getItem('token'));
  }

  private extractTokenFromHeader(response: HttpResponse<any>): string {
    const authHeader: string = response.headers.get('Authorization');
    return authHeader ? authHeader.replace('Bearer ', '') : null;
  }

  /**
   * @return Get token as string or null if no token exists.
   */
  public loadToken(): string {
    return localStorage.getItem(TOKEN_ID);
  }

  private storeToken(token: string): void {
    localStorage.setItem(TOKEN_ID, token);
  }

  public requestNewToken(): Observable<void> {
    return this.http.get('/api/v1/login/token', {}).pipe(map(() => null));
  }

  public startTokenAutoRefresh() {
    setInterval(() => {
      if (this.isAuthenticated()) {
        const token = this.getToken();
        if (Math.floor(new Date().getTime() / 1000) + 90 >= token.exp) {
          this.requestNewToken().subscribe(() => {});
        }
      }
    }, 60000);
  }

  useSessionToken(params: Params) {
    return this.http.get<any>('/classroom-api/join',
      {params: params, observe: 'response'})
      .pipe(map(res => {
        const token = this.extractTokenFromHeader(res);
        this.storeToken(token);
        return token;
      }), mergeMap(token => {
        const decodedToken = this.decodeToken(token);
        if (!decodedToken) {
          return throwError('Decoding the token failed');
        } else if (this.jwtHelper.isTokenExpired(token)) {
          return throwError('Token expired');
        }
        return of(decodedToken);
      }));
  }
}
