import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { RouterModule } from "@angular/router";

import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { MenuBarComponent} from "./page-components/menu-bar/menu-bar.component";
import { ConferenceComponent } from "./page-components/conference/conference.component";

// Material
// @ts-ignore
import { MaterialComponentsModule } from "./modules/material-components/material-components.module";

// Dialogs
import { NewConferenceDialogComponent } from './dialogs/newconference-dialog/new-conference-dialog.component';
import { NewTicketDialogComponent } from './dialogs/newticket-dialog/new-ticket-dialog.component';
import { IncomingCallDialogComponent } from './dialogs/incoming-call-dialog/incoming-call-dialog.component';
import { InviteToConferenceDialogComponent } from './dialogs/inviteto-conference-dialog/invite-to-conference-dialog.component';
import { AssignTicketDialogComponent } from './dialogs/assign-ticket-dialog/assign-ticket-dialog.component';
import {MatDialogModule} from "@angular/material/dialog";
import {MatSliderModule} from "@angular/material/slider";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {UserTeacherFilter} from "./pipes/user-teacher-filter";


@NgModule({
  declarations: [
    AppComponent,
    ConferenceComponent,
    // Dialogs
    NewConferenceDialogComponent,
    NewTicketDialogComponent,
    IncomingCallDialogComponent,
    InviteToConferenceDialogComponent,
    AssignTicketDialogComponent,
    MenuBarComponent,
    // Pipes
    UserTeacherFilter
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    RouterModule,
    // Material
    MaterialComponentsModule,
    MatDialogModule,
    MatSliderModule,
    ReactiveFormsModule,
    FormsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
