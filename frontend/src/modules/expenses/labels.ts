import { __ } from '../../lib/i18n';
import type { Tone } from '../../ui/Badge';
import { EntryState, EntryType, PaymentChannel } from './types';

export const CHANNEL_LABEL: Record<PaymentChannel, string> = {
  [PaymentChannel.CASH]: __('Cash'),
  [PaymentChannel.BANK]: __('Bank'),
};

export const STATE: Record<EntryState, { label: string; tone: Tone }> = {
  [EntryState.POSTED]: { label: __('Posted'), tone: 'green' },
  [EntryState.CANCELLED]: { label: __('Cancelled'), tone: 'gray' },
};

/** Copy that differs between expenses and other income. */
export const TYPE: Record<
  EntryType,
  { label: string; plural: string; overline: string; tone: Tone; newTitle: string; empty: string; hint: string; recorded: string }
> = {
  [EntryType.EXPENSE]: {
    label: __('Expense'),
    plural: __('Expenses'),
    overline: __('Expense'),
    tone: 'red',
    newTitle: __('New expense'),
    empty: __('Record your first expense'),
    hint: __('Rent, utilities, transport and other running costs paid in cash or by bank transfer.'),
    recorded: __('Expense recorded.'),
  },
  [EntryType.INCOME]: {
    label: __('Income'),
    plural: __('Other income'),
    overline: __('Other income'),
    tone: 'green',
    newTitle: __('New income'),
    empty: __('Record other income'),
    hint: __('Interest, services and other money received outside of sales.'),
    recorded: __('Income recorded.'),
  },
};
