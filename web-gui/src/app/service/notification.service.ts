import {Injectable, Injector} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Overlay, OverlayConfig} from "@angular/cdk/overlay";
import {ComponentPortal} from "@angular/cdk/portal";
import {OverlayErrorComponent} from "../page-components/overlay-error/overlay-error.component";

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  constructor(
    public snackBar: MatSnackBar,
    private overlay: Overlay,
    ) { }

  show(message: string, action: string = 'OK', seconds: number = 3): void {
    this.snackBar.open(message, undefined, { duration: seconds * 1000 });
  }

  showError(message: string, action: string = 'X', seconds: number = 3): void {
    this.snackBar.open(message, action, {panelClass: ['error'], duration: seconds * 1000});
  }

  public showOverlayError(message: string, heading: string = "") {
    let config = new OverlayConfig();

    config.positionStrategy = this.overlay.position()
      .global()
      .centerHorizontally()
      .centerVertically()
    config.hasBackdrop = true;
    config.backdropClass = "error-overlay-backdrop"
    config.panelClass = "error-overlay-panel"
    const overlayRef = this.overlay.create(config);

    const injector = Injector.create({
      providers: [
        {provide: 'message', useValue: message},
        {provide: 'heading', useValue: heading},
        {provide: 'overlayRef', useValue: overlayRef}
      ]
    });
    overlayRef.attach(new ComponentPortal(OverlayErrorComponent, null, injector));
  }
}
