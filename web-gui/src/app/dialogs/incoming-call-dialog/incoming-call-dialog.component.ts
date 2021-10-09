import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {ClassroomService} from '../../service/classroom.service';
import {InvitationEvent} from "../../rsocket/event/ClassroomEvent";

@Component({
  selector: 'app-incoming-call-dialog',
  templateUrl: './incoming-call-dialog.component.html',
  styleUrls: ['./incoming-call-dialog.component.scss']
})
export class IncomingCallDialogComponent implements OnInit {
  audio: HTMLAudioElement;
  constructor(public dialogRef: MatDialogRef<IncomingCallDialogComponent>,
              public classroomService: ClassroomService,
              @Inject(MAT_DIALOG_DATA) public invitation: InvitationEvent) { }

  ngOnInit(): void {
    const notification = new Notification('Konferenzeinladung',
      {body: this.invitation.inviter.fullName + 'lädt Sie zur Konferenz ' + this.invitation.conferenceInfo.conferenceName + 'ein!'});
    notification.onclick = () => window.focus();
    notification.onclose = () => window.focus();
    this.audio = new Audio();
    this.audio.src = '../../../../assets/classic_phone.mp3';
    this.audio.load();
    this.audio.play().then();
    this.dialogRef.afterClosed().subscribe(() => {
      this.audio.pause();
      this.audio.currentTime = 0;
    });
    document.addEventListener('visibilitychange', function() {
      if (document.visibilityState === 'visible') {
        // The tab has become visible so clear the now-stale Notification.
        notification.close();
      }
    });


  }
  // todo: fix string constants ( enum didnt work)
  public acceptCall() {
    this.classroomService.joinConference(this.invitation.conferenceInfo);
    this.dialogRef.close();
  }

  public declineCall() {
    this.dialogRef.close();
  }
}
