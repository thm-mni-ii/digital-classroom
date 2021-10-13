import {Component, Input} from '@angular/core';
import {ConferenceInfo} from "../../model/ConferenceInfo";
import {MatDialogRef} from "@angular/material/dialog";

@Component({
  selector: 'app-choose-conference-dialog',
  templateUrl: './choose-conference-dialog.component.html',
  styleUrls: ['./choose-conference-dialog.component.scss']
})
export class ChooseConferenceDialogComponent {
  @Input() conferences: ConferenceInfo[] | undefined;
  @Input() dialogRef: MatDialogRef<any> | undefined

  chooseConference(conference: ConferenceInfo) {
    if (this.dialogRef === undefined) {
      throw new Error("Dialog ref on ChooseConferenceDialog is undefined!")
    }
    this.dialogRef.close(conference)
  }
}
