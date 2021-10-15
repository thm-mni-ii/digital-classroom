import {Component, OnInit} from '@angular/core';
import {ConferenceInfo} from "../../../model/ConferenceInfo";
import {User} from "../../../model/User";
import {ClassroomService} from "../../../service/classroom.service";
import {
  CreateConferenceDialogComponent,
  CreateConferenceInputData
} from "../../../dialogs/create-conference-dialog/create-conference-dialog.component";
import {MatDialog} from "@angular/material/dialog";
import {filter} from "rxjs/operators";

@Component({
  selector: 'app-conference-list',
  templateUrl: './conference-list.component.html',
  styleUrls: ['./conference-list.component.scss']
})
export class ConferenceListComponent implements OnInit {

  conferences: ConferenceInfo[] | undefined
  currentUser: User | undefined

  constructor(
    private classroomService: ClassroomService,
    private dialog: MatDialog,
  ) { }

  ngOnInit(): void {
    this.classroomService.currentUserObservable.subscribe(
      currentUser => this.currentUser = currentUser
    )
    this.classroomService.conferencesObservable.subscribe(
      conferences => this.conferences = conferences
    )
  }

  public createConference() {
    this.dialog.open(CreateConferenceDialogComponent, {
      height: 'auto',
      width: 'auto',
      data: new CreateConferenceInputData(this.classroomService.classroomInfo!!, this.classroomService.currentUser!!)
    }).beforeClosed().pipe(
      filter(conferenceInfo => conferenceInfo instanceof ConferenceInfo),
    ).subscribe((conferenceInfo: ConferenceInfo) => {
      this.classroomService.createConference(conferenceInfo)
    });
  }

}
