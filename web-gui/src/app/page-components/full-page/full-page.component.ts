import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-full-page',
  templateUrl: './full-page.component.html',
  styleUrls: ['./full-page.component.scss']
})
export class FullPageComponent {

  @Input() icon: string
  @Input() title: string
  @Input() message: string
  @Input() footer: string

  constructor() {
  }

}
