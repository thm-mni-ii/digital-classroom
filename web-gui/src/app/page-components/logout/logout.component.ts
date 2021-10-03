import { Component, OnInit } from '@angular/core';
import {AuthService} from "../../service/auth.service";
import {ClassroomService} from "../../service/classroom.service";

@Component({
  selector: 'app-logout',
  templateUrl: './logout.component.html',
  styleUrls: ['./logout.component.scss']
})
export class LogoutComponent implements OnInit {

  constructor(
    private auth: AuthService,
    private classroomService: ClassroomService
  ) { }

  ngOnInit(): void {
    this.auth.logout()
    const logoutUrl = this.classroomService.classroomInfo.logoutUrl
    if (logoutUrl !== null) {
      window.open(logoutUrl, "_self");
    }
  }

}
