export const AccountKind = {
  ASSET: 'ASSET',
  LIABILITY: 'LIABILITY',
  EQUITY: 'EQUITY',
  REVENUE: 'REVENUE',
  EXPENSE: 'EXPENSE',
} as const;
export type AccountKind = (typeof AccountKind)[keyof typeof AccountKind];

export const EntryKind = {
  MANUAL: 'MANUAL',
  SALE: 'SALE',
  PAYMENT: 'PAYMENT',
  STOCK_ADJUSTMENT: 'STOCK_ADJUSTMENT',
  REVERSAL: 'REVERSAL',
  SALES_RETURN: 'SALES_RETURN',
  PURCHASE: 'PURCHASE',
  SUPPLIER_PAYMENT: 'SUPPLIER_PAYMENT',
  EXPENSE: 'EXPENSE',
  OTHER_INCOME: 'OTHER_INCOME',
  PAYROLL: 'PAYROLL',
} as const;
export type EntryKind = (typeof EntryKind)[keyof typeof EntryKind];

export interface Account {
  id: number;
  code: string;
  name: string;
  type: AccountKind;
  active: boolean;
}

export interface JournalLine {
  accountCode: string;
  accountName: string;
  debit: number;
  credit: number;
  partnerId: number | null;
  memo: string | null;
}

export interface JournalEntry {
  id: number;
  entryDate: string;
  reference: string | null;
  description: string | null;
  origin: EntryKind;
  reversed: boolean;
  reversalOf: number | null;
  createdBy: string;
  total: number;
  lines: JournalLine[];
}

export interface LedgerRow {
  entryId: number;
  date: string;
  reference: string | null;
  description: string | null;
  debit: number;
  credit: number;
  balance: number;
}

export interface TrialBalance {
  rows: { code: string; name: string; type: AccountKind; debit: number; credit: number }[];
  totalDebit: number;
  totalCredit: number;
}

export interface EntryInput {
  date: string;
  reference: string;
  description: string;
  lines: { accountCode: string; debit: number; credit: number; memo: string }[];
}

export interface PeriodLock {
  /** Entries dated on or before this date are locked; null while no period is closed. */
  lockDate: string | null;
  changedBy: string | null;
  changedAt: string | null;
}

/** Minimal view of a partner, fetched over HTTP so accounting has no code dependency on the partners module. */
export interface PartnerOption {
  id: number;
  name: string;
  customer: boolean;
  supplier: boolean;
  active: boolean;
}
