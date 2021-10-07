import { Component, OnInit } from '@angular/core';
import {AuthService} from "../../../service/auth.service";
import {LogoutService} from "../../../service/logout.service";

@Component({
  selector: 'app-logout',
  templateUrl: './logout.component.html',
  styleUrls: ['./logout.component.scss']
})
export class LogoutComponent implements OnInit {

  constructor(
    private auth: AuthService,
    private logoutService: LogoutService
  ) { }

  ngOnInit(): void {
    this.auth.logout()
    const logoutUrl = this.logoutService.classroomInfo.logoutUrl
    if (logoutUrl !== null) {
      window.open(logoutUrl, "_self");
    }
  }

}
