import {Component} from '@angular/core';
import {AssetManagerService} from "./util/asset-manager.service";

/**
 * Component that routes from login to app
 */
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  constructor(
    _assetManagerService: AssetManagerService
  ) {
  }
}
