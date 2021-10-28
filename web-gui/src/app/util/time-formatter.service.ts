import { Injectable } from '@angular/core';
import { format, formatDistanceToNowStrict, formatRelative } from 'date-fns';
import { de } from 'date-fns/locale';

@Injectable({
  providedIn: 'root',
})
export class TimeFormatterService {
  private formatDistanceLocale: any = {
    xSeconds: { one: 'gerade eben', other: 'gerade eben' },
    xMinutes: { one: 'vor {{count}} Minute', other: 'vor {{count}} Minuten' },
  };

  private formatRelativeLocal: any = {
    today: "'heute um' p",
    yesterday: "'gestern um' p",
    lastWeek: "dd. MMM 'um' p",
    other: "dd. MMM y 'um' p",
  };

  /**
   * Custom formatDistance function that
   * returns a string only if the distance is less than one hour
   */
  private formatDistance = (token: any, count: number) => {
    if (!Object.keys(this.formatDistanceLocale).includes(token)) return '';

    const res = this.formatDistanceLocale[token as any];

    if (count === 1) {
      return res.one.replace('{{count}}', count);
    }

    return res.other.replace('{{count}}', count);
  };

  private locale = {
    ...de,
    formatRelative: (token: any) => this.formatRelativeLocal[token],
    formatDistance: this.formatDistance,
  };

  /**
   * Calculates the elapsed time since the given date, and formats it as follows:
   *
   * - < 1 min = gerade eben
   * - < 1 h = ${minutes} Minuten
   * - today = heute um ${hh:mm}
   * - yesterday = gestern um ${hh:mm}
   * - last Week = ${dd. MM} um ${hh:mm}
   * - other = ${dd. MM y} um ${hh:mm}
   */
  public timeAgo(dateParam: number): string {
    const date = new Date(dateParam);

    const timeAgo = formatDistanceToNowStrict(date, {
      locale: { ...de, formatDistance: this.formatDistance },
    });

    if (timeAgo) return timeAgo;

    return formatRelative(date, new Date(), { locale: this.locale });
  }

  /** Formats the given date to dd. MM. y, hh:mm */
  public format(dateParam: number): string {
    return format(dateParam, 'dd. MMM y, hh:mm', { locale: de });
  }
}
