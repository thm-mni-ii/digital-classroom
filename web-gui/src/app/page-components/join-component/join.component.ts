import {Component, Inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {DOCUMENT} from '@angular/common';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {AuthService} from '../../service/auth.service';
import {JWTToken} from "../../model/JWTToken";

/**
 * Manages the login page for Submissionchecker
 */
@Component({
  selector: 'app-join',
  templateUrl: './join.component.html',
  styleUrls: ['./join.component.scss']
})
export class JoinComponent implements OnInit {
  username: string;
  password: string;

  constructor(private router: Router,
              private route: ActivatedRoute,
              private auth: AuthService,
              private dialog: MatDialog,
              @Inject(DOCUMENT) private document: Document,
              private snackbar: MatSnackBar) {
  }

  ngOnInit() {
    if (this.auth.isAuthenticated()) {
      console.log("Valid JWT found!")
      this.router.navigate(['/classroom'])
    } else {
      this.route.queryParams.subscribe(
        async params => {
          console.log('sessionToken: ', params["sessionToken"])
          await this.auth.useSessionToken(params).toPromise().then(
            token => console.log('user ', token.fullName, 'authenticated via sessionToken!')
          ).catch(
            reason => {
              console.log(reason)
              this.router.navigate(['/unauthorized'])
            }
          )
        }
      )
    }
  }
}
