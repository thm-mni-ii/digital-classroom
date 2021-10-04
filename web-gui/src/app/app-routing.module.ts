import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {ClassroomComponent} from "./page-components/classroom/classroom.component";
import {JoinComponent} from "./page-components/join-component/join.component";
import {UnauthorizedComponent} from "./page-components/unauthorized/unauthorized.component";
import {LogoutComponent} from "./page-components/logout/logout.component";
import {NotFoundComponent} from "./page-components/not-found/not-found.component";

const routes: Routes = [
  {path: '', component: ClassroomComponent},
  {path: 'classroom', component: ClassroomComponent},
  {path: 'classroom/join', component: JoinComponent},
  {path: 'logout', component: LogoutComponent},
  {path: 'unauthorized', component: UnauthorizedComponent},
  {path: '**', component: NotFoundComponent},
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
