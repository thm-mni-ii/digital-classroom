import {Component, Input, OnInit} from '@angular/core';
import {ConferenceInfo} from "../../../model/ConferenceInfo";
import {User} from "../../../model/User";

@Component({
  selector: 'app-conferences',
  templateUrl: './conferences.component.html',
  styleUrls: ['./conferences.component.scss']
})
export class ConferencesComponent {

  @Input() conferences: ConferenceInfo[]
  @Input() attendedConferences: ConferenceInfo[]
  @Input() currentUser: User

  constructor() {
  }



}
