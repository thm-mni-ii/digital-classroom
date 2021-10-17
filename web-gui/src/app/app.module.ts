import {ErrorHandler, NgModule} from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import {ClassroomComponent} from "./page-components/classroom/classroom.component";
import {MenuBarComponent} from "./page-components/menu-bar/menu-bar.component";
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {CreateEditTicketComponent} from "./dialogs/create-edit-ticket/create-edit-ticket.component";
import {InviteToConferenceDialogComponent} from "./dialogs/invite-to-conference-dialog/invite-to-conference-dialog.component";
import {MaterialComponentsModule} from "./modules/material-components/material-components.module";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatSliderModule} from "@angular/material/slider";
import {IncomingCallDialogComponent} from "./dialogs/incoming-call-dialog/incoming-call-dialog.component";
import {HttpClientModule} from "@angular/common/http";
import {JwtModule} from "@auth0/angular-jwt";
import {RouterModule} from "@angular/router";
import {JoinComponent} from "./page-components/join-component/join.component";
import { UnauthorizedComponent } from './page-components/full-page/unauthorized/unauthorized.component';
import {httpInterceptorProviders} from "./util/ApiURIHttpInterceptor";
import {IsNotSelfPipe } from './pipes/is-not-self.pipe';
import {IsPrivilegedPipe} from "./pipes/is-privileged-pipe";
import {UserListComponent} from "./page-components/classroom/user-list/user-list.component";
import {TicketListComponent} from "./page-components/classroom/ticket-list/ticket-list.component";
import {GlobalErrorHandler} from "./util/GlobalErrorHandler";
import { ConferenceListComponent } from './page-components/classroom/conference-list/conference-list.component';
import {CreateConferenceDialogComponent} from "./dialogs/create-conference-dialog/create-conference-dialog.component";
import {MatInputModule} from "@angular/material/input";
import {MatFormFieldModule} from "@angular/material/form-field";
import { LogoutComponent } from './page-components/full-page/logout/logout.component';
import {NotFoundComponent} from "./page-components/full-page/not-found/not-found.component";
import { OverlayErrorComponent } from './page-components/overlay-error/overlay-error.component';
import { ChooseConferenceDialogComponent } from './dialogs/choose-conference-dialog/choose-conference-dialog.component';
import {JoinUserConferenceDialogComponent} from "./dialogs/join-user-conference-dialog/join-user-conference-dialog.component";
import {AvatarModule} from "ngx-avatar";
import { FullPageComponent } from './page-components/full-page/full-page.component';
import { TicketComponent } from './page-components/classroom/ticket-list/ticket/ticket.component';
import { TicketAssignComponent } from './page-components/classroom/ticket-list/ticket/ticket-assign/ticket-assign.component';
import { SideDrawerMenuComponent } from './page-components/side-drawer-menu/side-drawer-menu.component';
import { ConferenceComponent } from './page-components/classroom/conference-list/conference/conference.component';
import { ConferenceAttendeesComponent } from './page-components/classroom/conference-list/conference/conference-attendees/conference-attendees.component';
import { DropdownMenuComponent } from './page-components/menu-bar/dropdown-menu/dropdown-menu.component';
import { TicketUserDisplayComponent } from './page-components/classroom/ticket-list/ticket/ticket-user-display/ticket-user-display.component';
import {LinkConferenceToTicketDialogComponent} from "./dialogs/link-conference-to-ticket-dialog/link-conference-to-ticket-dialog.component";


@NgModule({
  declarations: [
    AppComponent,
    ClassroomComponent,
    MenuBarComponent,
    CreateEditTicketComponent,
    InviteToConferenceDialogComponent,
    IsPrivilegedPipe,
    IncomingCallDialogComponent,
    JoinComponent,
    UnauthorizedComponent,
    IsNotSelfPipe,
    UserListComponent,
    TicketListComponent,
    NotFoundComponent,
    ConferenceListComponent,
    CreateConferenceDialogComponent,
    LogoutComponent,
    CreateConferenceDialogComponent,
    OverlayErrorComponent,
    ChooseConferenceDialogComponent,
    JoinUserConferenceDialogComponent,
    OverlayErrorComponent,
    FullPageComponent,
    TicketComponent,
    TicketAssignComponent,
    SideDrawerMenuComponent,
    ConferenceComponent,
    ConferenceAttendeesComponent,
    DropdownMenuComponent,
    TicketUserDisplayComponent,
    LinkConferenceToTicketDialogComponent
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
    }),
    AvatarModule
  ],
  providers: [httpInterceptorProviders, {provide: ErrorHandler, useClass: GlobalErrorHandler}],
  bootstrap: [AppComponent],
  entryComponents: [UserListComponent, ConferenceListComponent]
})
export class AppModule { }

export function tokenGetter() {
  return localStorage.getItem('token');
}
