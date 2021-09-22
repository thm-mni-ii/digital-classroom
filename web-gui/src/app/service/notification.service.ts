import { Injectable} from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  constructor(public snackBar: MatSnackBar) { }

  show(message: string, action: string = 'OK', seconds: number = 3): void {
    this.snackBar.open(message, null, { duration: seconds * 1000 });
  }

  showError(message: string, action: string = 'X', seconds: number = 3): void {
    this.snackBar.open(message, action, {panelClass: ['error'], duration: seconds * 1000});
  }
}
