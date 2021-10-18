import {Component, Input} from '@angular/core';
import {UserIconService} from "../../../../../util/user-icon.service";
import {User} from "../../../../../model/User";

@Component({
  selector: 'app-ticket-user-display',
  templateUrl: './ticket-user-display.component.html',
  styleUrls: ['./ticket-user-display.component.scss']
})
export class TicketUserDisplayComponent {

  @Input() user?: User
  @Input() label: string = ""

  constructor(
    public userIconService: UserIconService,
  ) {
  }

}
