import {
  HTTP_INTERCEPTORS,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
  HttpResponse
} from "@angular/common/http";
import {tap} from "rxjs/operators";
import {Observable} from "rxjs";
import {AuthService} from "../service/auth.service";
import {Injectable} from "@angular/core";

@Injectable()
export class ApiURIHttpInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService) {}
  public intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
      const idToken = this.authService.loadToken()
      if (idToken) {
        const cloned = req.clone({
          headers: req.headers.set("Authorization",
            "Bearer " + idToken)
        });
        return next.handle(cloned).pipe(tap(event => {
          if (event instanceof HttpResponse) {
            const response = <HttpResponse<any>>event;
            this.authService.renewToken(response);
          }
        })
        )
      }
      else {
        return next.handle(req);
      }
    }
}

export const httpInterceptorProviders = [
  {provide: HTTP_INTERCEPTORS, useClass: ApiURIHttpInterceptor, multi: true}
];
