import {Component, Inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {DOCUMENT} from '@angular/common';
import {MatDialog} from '@angular/material/dialog';
import {AuthService} from '../../service/auth.service';

/**
 * Manages the login page for Submissionchecker
 */
@Component({
  selector: 'app-join',
  templateUrl: './join.component.html',
  styleUrls: ['./join.component.scss']
})
export class JoinComponent implements OnInit {
  constructor(private router: Router,
              private route: ActivatedRoute,
              private auth: AuthService,
              private dialog: MatDialog,
              @Inject(DOCUMENT) private document: Document) {
  }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
        console.log('sessionToken: ', params["sessionToken"])
        this.auth.useSessionToken(params).subscribe(token => {
            console.log('user ', token.fullName, 'authenticated via sessionToken!')
            this.router.navigate(['/classroom']).then()
          }, error => {
          console.log(error)
          if (!this.auth.isAuthenticated()) {
            this.router.navigate(['/unauthorized']).then()
            return;
          }
        })
    })
  }
}
