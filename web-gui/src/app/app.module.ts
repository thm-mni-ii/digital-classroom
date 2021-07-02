import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import {ClassroomComponent} from "./page-components/conference/classroom.component";
import {MenuBarComponent} from "./page-components/menu-bar/menu-bar.component";
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {NewTicketDialogComponent} from "./dialogs/newticket-dialog/new-ticket-dialog.component";
import {NewConferenceDialogComponent} from "./dialogs/newconference-dialog/new-conference-dialog.component";
import {InviteToConferenceDialogComponent} from "./dialogs/inviteto-conference-dialog/invite-to-conference-dialog.component";
import {AssignTicketDialogComponent} from "./dialogs/assign-ticket-dialog/assign-ticket-dialog.component";
import {UserTeacherFilter} from "./dialogs/pipes/user-teacher-filter";
import {MaterialComponentsModule} from "./modules/material-components/material-components.module";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatSliderModule} from "@angular/material/slider";
import {IncomingCallDialogComponent} from "./dialogs/incoming-call-dialog/incoming-call-dialog.component";

@NgModule({
  declarations: [
    AppComponent,
    ClassroomComponent,
    MenuBarComponent,
    NewTicketDialogComponent,
    NewConferenceDialogComponent,
    InviteToConferenceDialogComponent,
    AssignTicketDialogComponent,
    UserTeacherFilter,
    IncomingCallDialogComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MaterialComponentsModule,
    FormsModule,
    ReactiveFormsModule,
    MatSliderModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
