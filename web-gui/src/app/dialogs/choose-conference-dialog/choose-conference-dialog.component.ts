import {Component, Input} from '@angular/core';
import {ConferenceInfo} from "../../model/ConferenceInfo";
import {MatDialogRef} from "@angular/material/dialog";

@Component({
  selector: 'app-choose-conference-dialog',
  templateUrl: './choose-conference-dialog.component.html',
  styleUrls: ['./choose-conference-dialog.component.scss']
})
export class ChooseConferenceDialogComponent {
  @Input() conferences: ConferenceInfo[];
  @Input() dialogRef: MatDialogRef<any>

  constructor(
  ) { }


  chooseConference(conference: ConferenceInfo) {
    this.dialogRef.close(conference)
  }
}
