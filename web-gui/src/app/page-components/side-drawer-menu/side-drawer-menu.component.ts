import {Component, Input, Type} from '@angular/core';

@Component({
  selector: 'app-side-drawer-menu',
  templateUrl: './side-drawer-menu.component.html',
  styleUrls: ['./side-drawer-menu.component.scss']
})
export class SideDrawerMenuComponent {

  opened: boolean;
  innerWidth: number;
  @Input() label: string = "Teilnehmer";
  @Input() orientation: string = "left"
  @Input() component: Type<any> | undefined

  constructor() {
    this.opened = true;
    this.innerWidth = window.innerWidth;
  }

  get showSidebarMenu() {
    return this.innerWidth <= 400;
  }

  onResize(event: UIEvent) {
    this.innerWidth = (event.target as Window).innerWidth;
  }

  icon() {
    if ((this.opened && this.orientation === "left") || (!this.opened && this.orientation === "right")) return "keyboard_arrow_left"
    else if ((!this.opened && this.orientation === "left") || (this.opened && this.orientation === "right")) return "keyboard_arrow_right"
    else return "error"
  }

  getPosition(): "start" | "end" {
    if (this.orientation == "left") return "start"
    else return "end"
  }

  assignClassContent(): string {
    return 'sidenav-content-' + this.orientation
  }

  assignClass(): string {
    return 'sidenav-container-' + this.orientation
  }
}
