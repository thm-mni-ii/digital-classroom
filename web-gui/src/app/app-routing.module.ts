import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {ClassroomComponent} from "./page-components/conference/classroom.component";
import {AuthGuard} from "./guards/auth.guard";

const routes: Routes = [
  {path: 'classroom', component: ClassroomComponent, canActivate: [AuthGuard]},
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
