import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class UserIconService {

  private colors: string[] = ['#FD9A63', '#60CB7E', '#26B8B8', '#405E9A']

  constructor() { }

  public calculateColorClass(name: string): string {
    return this.colors[UserIconService.hashCode(name) % this.colors.length];
  }

  private static hashCode(str: string) {
    let hash = 0, i, chr;
    if (str.length === 0) return hash;
    for (i = 0; i < str.length; i++) {
      chr   = str.charCodeAt(i);
      hash  = ((hash << 5) - hash) + chr;
      hash |= 0; // Convert to 32bit integer
    }
    return hash;
  };
}
