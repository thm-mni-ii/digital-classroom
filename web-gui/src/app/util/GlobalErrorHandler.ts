import {ErrorHandler, Injectable} from '@angular/core';
import {NotificationService} from "../service/notification.service";

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {

  constructor(private notification: NotificationService) {
  }

  handleError(error: Error) {
    this.notification.showError(error.message)
    console.error(error)
  }

}
