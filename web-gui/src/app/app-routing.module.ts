import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {ClassroomComponent} from "./page-components/classroom/classroom.component";
import {AuthGuard} from "./guards/auth.guard";
import {JoinComponent} from "./page-components/join-component/join.component";
import {UnauthorizedComponent} from "./page-components/unauthorized/unauthorized.component";

const routes: Routes = [
  {path: 'classroom/join', component: JoinComponent},
  {path: 'classroom', component: ClassroomComponent},
  {path: '**', component: UnauthorizedComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
