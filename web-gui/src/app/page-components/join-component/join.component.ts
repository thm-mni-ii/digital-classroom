import {Component, Inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {DOCUMENT} from '@angular/common';
import {MatDialog} from '@angular/material/dialog';
import {AuthService} from '../../service/auth.service';
import {map, mergeMap} from "rxjs/operators";
import {HttpErrorResponse} from "@angular/common/http";

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
    this.route.queryParamMap.pipe(
      map(paramMap => paramMap.get("sessionToken") == null ? "" : paramMap.get("sessionToken")),
      mergeMap(token => this.auth.useSessionToken(token!!)),
    ).subscribe( ok => {
      console.log(ok)
        this.router.navigate(['/']).then()
      }, (error: HttpErrorResponse) => {
        if (error.status === 403 && this.auth.isAuthenticated()) {
          console.log("token invalid, valid jwt!")
          this.router.navigate(['/']).then()
        } else {
          this.auth.logout()
          this.router.navigate(['/']).then()
        }
      }
    )
  }
}
