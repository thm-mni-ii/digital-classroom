import {Component} from '@angular/core';
import {MatIconRegistry} from "@angular/material/icon";
import {DomSanitizer} from "@angular/platform-browser";

/**
 * Component that routes from login to app
 */
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  constructor(private matIconRegistry: MatIconRegistry, private domSanitizer: DomSanitizer) {
    this.matIconRegistry.addSvgIcon('add-ticket', this.domSanitizer.bypassSecurityTrustResourceUrl('../assets/add-ticket.svg'));
    this.matIconRegistry.addSvgIcon('invite', this.domSanitizer.bypassSecurityTrustResourceUrl('../assets/invite.svg'));
  }
}
