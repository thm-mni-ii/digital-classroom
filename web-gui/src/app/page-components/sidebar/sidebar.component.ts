import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {TitlebarService} from '../../service/titlebar.service';
import {Observable, of} from 'rxjs';

/**
 * Root component shows sidenav and titlebar
 */
@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent implements OnInit {
  constructor(private router: Router,
    private titlebar: TitlebarService) {
  }

  title: Observable<string> = of('');
  opened: boolean;
  innerWidth: number;

  username: string;
  isAdmin: boolean;
  isModerator: boolean;

  ngOnInit() {
    this.opened = true;


    this.title = this.titlebar.getTitle();
    this.innerWidth = window.innerWidth;
  }

  /**
   * Deletes cookie and jwt after that user gets logged out
   */
  logout() {
    this.router.navigate(['login']);
  }

  get showSidebarMenu() {
    return this.innerWidth <= 400;
  }

  /**
   * Listen to onResize and update sidebar visibility settings
   * @param event
   */
  onResize(event) {
    this.innerWidth = event.target.innerWidth;
  }
}
