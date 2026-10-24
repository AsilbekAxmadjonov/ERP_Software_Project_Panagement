export const EntryType = { EXPENSE: 'EXPENSE', INCOME: 'INCOME' } as const;
export type EntryType = (typeof EntryType)[keyof typeof EntryType];

export const PaymentChannel = { CASH: 'CASH', BANK: 'BANK' } as const;
export type PaymentChannel = (typeof PaymentChannel)[keyof typeof PaymentChannel];

export const EntryState = { POSTED: 'POSTED', CANCELLED: 'CANCELLED' } as const;
export type EntryState = (typeof EntryState)[keyof typeof EntryState];

export interface Category {
  id: number;
  name: string;
  type: EntryType;
  accountCode: string;
  active: boolean;
}

export interface LedgerAccount {
  code: string;
  name: string;
}

export interface CashEntry {
  id: number;
  /** Document reference used in the ledger, e.g. EXP-00012. */
  number: string;
  type: EntryType;
  categoryId: number;
  categoryName: string;
  accountCode: string;
  date: string;
  amount: number;
  method: PaymentChannel;
  description: string | null;
  status: EntryState;
  cancelledOn: string | null;
  createdBy: string;
}

export interface CategoryTotal {
  categoryId: number;
  categoryName: string;
  accountCode: string;
  count: number;
  total: number;
}

export interface CategoryReport {
  from: string | null;
  to: string | null;
  expenses: CategoryTotal[];
  income: CategoryTotal[];
  totalExpenses: number;
  totalIncome: number;
  net: number;
}

export interface CategoryCreateInput {
  name: string;
  type: EntryType;
  accountCode: string;
}

export interface CategoryUpdateInput {
  name: string;
  accountCode: string;
  active: boolean;
}

export interface CashEntryInput {
  type: EntryType;
  categoryId: number;
  date: string;
  amount: number;
  method: PaymentChannel;
  description: string;
}
