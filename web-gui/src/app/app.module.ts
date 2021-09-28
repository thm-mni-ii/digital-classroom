import {ErrorHandler, NgModule} from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import {ClassroomComponent} from "./page-components/classroom/classroom.component";
import {MenuBarComponent} from "./page-components/menu-bar/menu-bar.component";
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {NewTicketDialogComponent} from "./dialogs/new-ticket-dialog/new-ticket-dialog.component";
import {InviteToConferenceDialogComponent} from "./dialogs/invite-to-conference-dialog/invite-to-conference-dialog.component";
import {AssignTicketDialogComponent} from "./dialogs/assign-ticket-dialog/assign-ticket-dialog.component";
import {MaterialComponentsModule} from "./modules/material-components/material-components.module";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatSliderModule} from "@angular/material/slider";
import {IncomingCallDialogComponent} from "./dialogs/incoming-call-dialog/incoming-call-dialog.component";
import {HttpClientModule} from "@angular/common/http";
import {JwtModule} from "@auth0/angular-jwt";
import {RouterModule} from "@angular/router";
import {JoinComponent} from "./page-components/join-component/join.component";
import { UnauthorizedComponent } from './page-components/unauthorized/unauthorized.component';
import {httpInterceptorProviders} from "./util/ApiURIHttpInterceptor";
import {IsNotSelfPipe } from './pipes/is-not-self.pipe';
import {IsPrivilegedPipe} from "./pipes/is-privileged-pipe";
import {UserListComponent} from "./page-components/classroom/user-list/user-list.component";
import {TicketListComponent} from "./page-components/classroom/ticket-list/ticket-list.component";
import {GlobalErrorHandler} from "./util/GlobalErrorHandler";
import { ConferencesComponent } from './page-components/classroom/conferences/conferences.component';
import {CreateConferenceDialogComponent} from "./dialogs/create-conference-dialog/create-conference-dialog.component";
import {MatInputModule} from "@angular/material/input";
import {MatFormFieldModule} from "@angular/material/form-field";

@NgModule({
  declarations: [
    AppComponent,
    ClassroomComponent,
    MenuBarComponent,
    NewTicketDialogComponent,
    InviteToConferenceDialogComponent,
    AssignTicketDialogComponent,
    IsPrivilegedPipe,
    IncomingCallDialogComponent,
    JoinComponent,
    UnauthorizedComponent,
    IsNotSelfPipe,
    UserListComponent,
    TicketListComponent,
    ConferencesComponent,
    CreateConferenceDialogComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MaterialComponentsModule,
    FormsModule,
    ReactiveFormsModule,
    MatSliderModule,
    HttpClientModule,
    RouterModule,
    MatFormFieldModule,
    MatInputModule,
    JwtModule.forRoot({
      config: {
        tokenGetter: tokenGetter,
      }
    })
  ],
  providers: [httpInterceptorProviders, {provide: ErrorHandler, useClass: GlobalErrorHandler}],
  bootstrap: [AppComponent]
})
export class AppModule { }

export function tokenGetter() {
  return localStorage.getItem('token');
}
