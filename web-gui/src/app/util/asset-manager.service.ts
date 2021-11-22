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
    this.matIconRegistry.addSvgIcon('create-conference', this.domSanitizer.bypassSecurityTrustResourceUrl('assets/create-conference.svg'));
    this.matIconRegistry.addSvgIcon('link-conference', this.domSanitizer.bypassSecurityTrustResourceUrl('assets/link-conference.svg'));
    this.matIconRegistry.addSvgIcon('join-conference', this.domSanitizer.bypassSecurityTrustResourceUrl('assets/join-conference.svg'));
    this.matIconRegistry.addSvgIcon('invite-conference', this.domSanitizer.bypassSecurityTrustResourceUrl('assets/invite-conference.svg'));
    this.matIconRegistry.addSvgIcon('create-plenary-conf', this.domSanitizer.bypassSecurityTrustResourceUrl('assets/create-plenary-conf.svg'));

    this.assets.set("ringtone", 'assets/classic_phone.mp3')
    this.assets.set("notification", 'assets/notification.mp3')

  }

  public getAsset(name: string): string {
    const asset = this.assets.get(name);
    if (asset === undefined) {
      throw Error('Asset ' + name + ' not found!')
    }
    return asset
  }
}
