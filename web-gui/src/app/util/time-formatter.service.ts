/**
 * Quelle: https://muffinman.io/blog/javascript-time-ago-function/
 */

import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class TimeFormatterService {

  constructor() { }

  private months: string[] = [
    'Januar', 'Februar', 'März', 'April', 'Mai', 'Juni',
    'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember'
  ];

  // @ts-ignore
  private getFormattedDate(date, prefomattedDate: string = undefined, hideYear = false) {
    const day = date.getDate();
    const month = this.months[date.getMonth()];
    const year = date.getFullYear();
    const hours = date.getHours();
    let minutes = date.getMinutes();

    if (minutes < 10) {
      // Adding leading zero to minutes
      minutes = `0${ minutes }`;
    }

    if (prefomattedDate) {
      // Today at 10:20
      // Yesterday at 10:20
      return `${ prefomattedDate } um ${ hours }:${ minutes }`;
    }

    if (hideYear) {
      // 10. January at 10:20
      return `${ day }. ${ month } um ${ hours }:${ minutes }`;
    }

    // 10. January 2017. at 10:20
    return `${ day }. ${ month } ${ year }. um ${ hours }:${ minutes }`;
  }


// --- Main function
// @ts-ignore
  public timeAgo(dateParam): string {
    if (!dateParam) {
      // @ts-ignore
      return null;
    }

    const date = typeof dateParam === 'object' ? dateParam : new Date(dateParam);
    const DAY_IN_MS = 86400000; // 24 * 60 * 60 * 1000
    const today: number = Date.now()
    const yesterday = new Date(today - DAY_IN_MS);
    const seconds = Math.round((today - date) / 1000);
    const minutes = Math.round(seconds / 60);
    const isToday = new Date(today).toDateString() === date.toDateString();
    const isYesterday = yesterday.toDateString() === date.toDateString();
    const isThisYear = new Date(today).getFullYear() === date.getFullYear();


    if (seconds < 30) {
      return 'gerade eben';
    //} else if (seconds < 30) {
    // return `vor ${ seconds } Sek.`;
    } else if (seconds < 90) {
      return 'vor 1 Minute';
    } else if (minutes < 60) {
      return `vor ${ minutes } Minuten`;
    } else if (isToday) {
      return this.getFormattedDate(date, 'Heute'); // Today at 10:20
    } else if (isYesterday) {
      return this.getFormattedDate(date, 'Gestern'); // Yesterday at 10:20
    } else if (isThisYear) {
      return this.getFormattedDate(date, undefined, true); // 10. January at 10:20
    }

    return this.getFormattedDate(date); // 10. January 2017. at 10:20
  }
}
