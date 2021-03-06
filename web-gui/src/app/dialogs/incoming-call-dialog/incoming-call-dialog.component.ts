import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {ClassroomService} from '../../service/classroom.service';
import {AssetManagerService} from "../../util/asset-manager.service";
import {Howl} from "howler";
import {InvitationEvent} from "../../rsocket/event/InvitationEvent";

@Component({
  selector: 'app-incoming-call-dialog',
  templateUrl: './incoming-call-dialog.component.html',
  styleUrls: ['./incoming-call-dialog.component.scss']
})
export class IncomingCallDialogComponent implements OnInit {

  private sound = new Howl({
    src: [this.assetManager.getAsset("ringtone")],
    loop: true,
    volume: 0.5
  })
  invitationText: string = ""

  constructor(public dialogRef: MatDialogRef<IncomingCallDialogComponent>,
              public classroomService: ClassroomService,
              private assetManager: AssetManagerService,
              @Inject(MAT_DIALOG_DATA) public invitation: InvitationEvent) { }

  ngOnInit(): void {
    if (this.invitation.conferenceInfo === undefined) throw new Error("Conference invited to is undefined!")
    if (this.invitation.inviter === undefined) this.invitationText = "Bitte dem Plenum beitreten: " + this.invitation.conferenceInfo.conferenceName
    else this.invitationText = this.invitation.inviter.fullName + 'lädt Sie zu ' + this.invitation.conferenceInfo.conferenceName + 'ein!'


    const notification = new Notification('Konferenzeinladung',
      {body: this.invitationText});
    notification.onclick = () => window.focus();
    notification.onclose = () => this.sound.stop();

    this.sound.play()
    this.dialogRef.afterClosed().subscribe(() => {
      this.sound.stop();
    });
    document.addEventListener('visibilitychange', function() {
      if (document.visibilityState === 'visible') {
        // The tab has become visible so clear the now-stale Notification.
        notification.close();
      }
    });
  }

  public acceptCall() {
    this.dialogRef.close(true);
  }

  public declineCall() {
    this.dialogRef.close(false);
  }
}
