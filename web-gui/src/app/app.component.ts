/**
 * Code for utilitiesService and documentClick() from user ginalx at https://stackoverflow.com/a/59234391
 * Published under CC License 4.0: https://creativecommons.org/licenses/by-sa/4.0/
 */

import {Component, HostListener, Injectable} from '@angular/core';
import {Subject} from "rxjs";
import {AssetManagerService} from "./util/asset-manager.service";

@Component({
  selector: 'app-root',
  template: '<router-outlet></router-outlet>'
})
export class AppComponent {

  constructor(
    private utilitiesService: UtilitiesService,
    private _assetManagerService: AssetManagerService
  ) {}

  @HostListener('document:click', ['$event'])
  documentClick(event: any): void {
    this.utilitiesService.documentClickedTarget.next(event.target)
  }
}

@Injectable({ providedIn: 'root' })
export class UtilitiesService {
  documentClickedTarget: Subject<HTMLElement> = new Subject<HTMLElement>()
}
