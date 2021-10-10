import {Component, Input, OnInit, ViewChild, ViewContainerRef} from '@angular/core';

@Component({
  selector: 'app-side-drawer-menu',
  templateUrl: './side-drawer-menu.component.html',
  styleUrls: ['./side-drawer-menu.component.scss']
})
export class SideDrawerMenuComponent implements OnInit {

  opened: boolean;
  innerWidth: number;
  @Input() label: string = "Teilnehmer";
  @Input() orientation: string = "left"

  constructor() { }

  ngOnInit(): void {
    this.opened = true;
    this.innerWidth = window.innerWidth;
  }

  get showSidebarMenu() {
    return this.innerWidth <= 400;
  }

  onResize(event) {
    this.innerWidth = event.target.innerWidth;
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
    if (this.orientation == 'left') return 'mat-sidenav-content-left'
    else return 'mat-sidenav-content-right'
  }

  assignClass(): string {
    if (this.orientation == 'left') return 'mat-sidenav-left'
    else return 'mat-sidenav-right'
  }
}
