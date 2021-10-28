import { Injectable } from '@angular/core';
import { formatDistanceToNowStrict, formatRelative } from 'date-fns';
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
    other: "dd. MMM 'um' p",
  };

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

  public timeAgo(dateParam: number): string {
    const date = new Date(dateParam);

    const timeAgo = formatDistanceToNowStrict(date, {
      locale: { ...de, formatDistance: this.formatDistance },
    });

    if (timeAgo) return timeAgo;

    return formatRelative(date, new Date(), { locale: this.locale });
  }
}
