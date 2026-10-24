import { __ } from '../../lib/i18n';
import type { Tone } from '../../ui/Badge';
import { AccountKind, EntryKind } from './types';

export const KIND_LABEL: Record<AccountKind, string> = {
  [AccountKind.ASSET]: __('Asset'),
  [AccountKind.LIABILITY]: __('Liability'),
  [AccountKind.EQUITY]: __('Equity'),
  [AccountKind.REVENUE]: __('Revenue'),
  [AccountKind.EXPENSE]: __('Expense'),
};

export const ORIGIN: Record<EntryKind, { label: string; tone: Tone }> = {
  [EntryKind.MANUAL]: { label: __('Manual'), tone: 'gray' },
  [EntryKind.SALE]: { label: __('Sale'), tone: 'green' },
  [EntryKind.PAYMENT]: { label: __('Payment'), tone: 'blue' },
  [EntryKind.STOCK_ADJUSTMENT]: { label: __('Stock adjustment'), tone: 'yellow' },
  [EntryKind.REVERSAL]: { label: __('Reversal'), tone: 'red' },
  [EntryKind.SALES_RETURN]: { label: __('Credit note'), tone: 'yellow' },
  [EntryKind.PURCHASE]: { label: __('Purchase'), tone: 'purple' },
  [EntryKind.SUPPLIER_PAYMENT]: { label: __('Vendor payment'), tone: 'blue' },
  [EntryKind.EXPENSE]: { label: __('Expense'), tone: 'red' },
  [EntryKind.OTHER_INCOME]: { label: __('Other income'), tone: 'green' },
  [EntryKind.PAYROLL]: { label: __('Payroll'), tone: 'purple' },
};

/** Cash flow groups by entry origin; an origin the backend adds before this table is updated shows its raw name. */
export const originLabel = (kind: string): string => (ORIGIN as Partial<Record<string, { label: string }>>)[kind]?.label ?? kind;
