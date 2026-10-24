import { __ } from '../../lib/i18n';
import type { Tone } from '../../ui/Badge';
import { EmployeeState, LedgerEntryKind, PaymentChannel } from './types';

export const STATE: Record<EmployeeState, { label: string; tone: Tone }> = {
  [EmployeeState.ACTIVE]: { label: __('Active'), tone: 'green' },
  [EmployeeState.INACTIVE]: { label: __('Inactive'), tone: 'gray' },
};

export const CHANNEL_LABEL: Record<PaymentChannel, string> = {
  [PaymentChannel.CASH]: __('Cash'),
  [PaymentChannel.BANK]: __('Bank'),
};

/** paidOut: money leaves cash or bank, so the dialog asks for the journal. */
export const ENTRY: Record<LedgerEntryKind, { label: string; action: string; tone: Tone; paidOut: boolean; hint: string; done: string }> = {
  [LedgerEntryKind.SALARY_ACCRUED]: {
    label: __('Salary accrued'),
    action: __('Accrue salary'),
    tone: 'purple',
    paidOut: false,
    hint: __('Records the month’s salary as owed to the employee (salary expense against salaries payable).'),
    done: __('Salary accrued.'),
  },
  [LedgerEntryKind.BONUS]: {
    label: __('Bonus'),
    action: __('Bonus'),
    tone: 'green',
    paidOut: false,
    hint: __('Adds a bonus to what the employee is owed.'),
    done: __('Bonus recorded.'),
  },
  [LedgerEntryKind.PAYMENT]: {
    label: __('Payment'),
    action: __('Pay'),
    tone: 'blue',
    paidOut: true,
    hint: __('Pays out what the employee is owed in cash or by bank transfer.'),
    done: __('Payment recorded.'),
  },
  [LedgerEntryKind.ADVANCE]: {
    label: __('Advance'),
    action: __('Advance'),
    tone: 'yellow',
    paidOut: true,
    hint: __('Pays money ahead of salary; the employee’s balance may become negative until salary is accrued.'),
    done: __('Advance recorded.'),
  },
  [LedgerEntryKind.DEDUCTION]: {
    label: __('Deduction'),
    action: __('Deduction'),
    tone: 'red',
    paidOut: false,
    hint: __('Reduces what the employee is owed, e.g. a penalty or a recovered cost.'),
    done: __('Deduction recorded.'),
  },
};
