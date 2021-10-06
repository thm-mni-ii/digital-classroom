import { Injectable } from '@angular/core';
import {ClassroomInfo} from "../model/ClassroomInfo";

@Injectable({
  providedIn: 'root'
})
export class LogoutService {

  private _classroomInfo: ClassroomInfo = new ClassroomInfo()

  constructor() { }

  get classroomInfo(): ClassroomInfo {
    return this._classroomInfo;
  }

  set classroomInfo(value: ClassroomInfo) {
    this._classroomInfo = value;
  }
}
