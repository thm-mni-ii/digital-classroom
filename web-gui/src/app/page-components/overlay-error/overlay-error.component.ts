import {Component, Inject} from '@angular/core';

@Component({
  selector: 'app-overlay-error',
  templateUrl: './overlay-error.component.html',
  styleUrls: ['./overlay-error.component.scss']
})
export class OverlayErrorComponent {

  constructor(
    @Inject('message') public message: string,
    @Inject('heading') public heading: string = 'Verbindung getrennt!'
  ) { }

}
