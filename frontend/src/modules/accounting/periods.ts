import { __ } from '../../lib/i18n';

export interface DateRange {
  from: string;
  to: string;
}

export type PeriodPreset = 'month' | 'lastMonth' | 'quarter' | 'year';

const iso = (d: Date): string => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;

/** Ranges in the user's local calendar; each ends today unless it lies wholly in the past. */
export const PERIOD_PRESETS: Record<PeriodPreset, { label: string; range: () => DateRange }> = {
  month: {
    label: __('This month'),
    range: () => {
      const now = new Date();
      return { from: iso(new Date(now.getFullYear(), now.getMonth(), 1)), to: iso(now) };
    },
  },
  lastMonth: {
    label: __('Last month'),
    range: () => {
      const now = new Date();
      return { from: iso(new Date(now.getFullYear(), now.getMonth() - 1, 1)), to: iso(new Date(now.getFullYear(), now.getMonth(), 0)) };
    },
  },
  quarter: {
    label: __('This quarter'),
    range: () => {
      const now = new Date();
      return { from: iso(new Date(now.getFullYear(), Math.floor(now.getMonth() / 3) * 3, 1)), to: iso(now) };
    },
  },
  year: {
    label: __('This year'),
    range: () => {
      const now = new Date();
      return { from: iso(new Date(now.getFullYear(), 0, 1)), to: iso(now) };
    },
  },
};
