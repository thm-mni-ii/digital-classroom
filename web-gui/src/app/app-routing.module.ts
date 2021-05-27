import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ConferenceComponent} from './page-components/conference/conference.component';
import {NotFoundComponent} from "./page-components/not-found/not-found.component";
import {SidebarComponent} from "./page-components/sidebar/sidebar.component";
import {DummyComponent} from "./page-components/dummy/dummy.component";


const routes: Routes = [
  //{path: 'login', component: LoginComponent},
  {path: '', component: SidebarComponent},
  {path: 'conf', component: ConferenceComponent},
  {path: 'dummy', component: DummyComponent},
  {path: '404', component: NotFoundComponent},
  {path: '**', redirectTo: '404' }
];

/**
 * Routing of angular app
 */
@NgModule({
  imports: [
    RouterModule.forRoot(routes, {enableTracing: false})
  ],
  exports: [
    RouterModule
  ],
})
export class AppRoutingModule {
}

