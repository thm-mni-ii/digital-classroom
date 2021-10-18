import {Component, Inject} from '@angular/core';
import {Router} from "@angular/router";
import {OverlayRef} from "@angular/cdk/overlay";

@Component({
  selector: 'app-overlay-error',
  templateUrl: './overlay-error.component.html',
  styleUrls: ['./overlay-error.component.scss']
})
export class OverlayErrorComponent {

  constructor(
    @Inject('message') public message: string,
    @Inject('heading') public heading: string = 'Verbindung getrennt!',
    @Inject('overlayRef') private overlayRef: OverlayRef,
    private router: Router
  ) { }

  refresh() {
    window.location.reload()
  }

  exit() {
    this.overlayRef.dispose()
    this.router.navigate(["/logout"]).then()
  }
}
