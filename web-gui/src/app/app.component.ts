import {Component, OnInit} from '@angular/core';
import {AuthService} from './service/auth.service';
import {environment} from "../environments/environment";

/**
 * Component that routes from login to app
 */
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  constructor(
    private authService: AuthService
  ) {
    console.log(environment.production)
  }

  ngOnInit(): void {
    this.authService.startTokenAutoRefresh();
  }
}
