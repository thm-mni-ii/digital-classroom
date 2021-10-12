import {Injectable} from '@angular/core';
import {MatIconRegistry} from "@angular/material/icon";
import {DomSanitizer} from "@angular/platform-browser";

@Injectable({
  providedIn: 'root'
})
export class AssetManagerService {

  private assets: Map<string, string> = new Map<string, string>()

  constructor(
    private matIconRegistry: MatIconRegistry,
    private domSanitizer: DomSanitizer
  ) {
    this.matIconRegistry.addSvgIcon('add-ticket', this.domSanitizer.bypassSecurityTrustResourceUrl('assets/add-ticket.svg'));
    this.matIconRegistry.addSvgIcon('invite', this.domSanitizer.bypassSecurityTrustResourceUrl('assets/invite.svg'));
    this.assets.set("ringtone", 'assets/classic_phone.mp3')
  }

  public getAsset(name: string): string {
    return this.assets.get(name);
  }
}
