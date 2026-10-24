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

/** One account on a financial statement; amount is on the side where the account kind grows. */
export interface StatementLine {
  code: string;
  name: string;
  type: AccountKind;
  amount: number;
}

export interface ProfitAndLoss {
  from: string;
  to: string;
  revenue: StatementLine[];
  totalRevenue: number;
  expenses: StatementLine[];
  totalExpenses: number;
  netProfit: number;
}

export interface BalanceSheet {
  asOf: string;
  assets: StatementLine[];
  totalAssets: number;
  liabilities: StatementLine[];
  totalLiabilities: number;
  equity: StatementLine[];
  /** Revenue minus expenses not yet closed to an equity account; already included in totalEquity. */
  currentEarnings: number;
  totalEquity: number;
  totalLiabilitiesAndEquity: number;
  balanced: boolean;
}

export interface CashFlow {
  from: string;
  to: string;
  accounts: { code: string; name: string; opening: number; inflow: number; outflow: number; closing: number }[];
  opening: number;
  inflow: number;
  outflow: number;
  closing: number;
  inflowsByOrigin: CashFlowOrigin[];
  outflowsByOrigin: CashFlowOrigin[];
  days: { date: string; inflow: number; outflow: number; closing: number }[];
  movements: CashMovement[];
}

export interface CashFlowOrigin {
  origin: EntryKind;
  amount: number;
  /** Number of journal entries behind the amount. */
  entries: number;
}

export interface CashMovement {
  entryId: number;
  date: string;
  reference: string | null;
  description: string | null;
  origin: EntryKind;
  accountCode: string;
  accountName: string;
  inflow: number;
  outflow: number;
  /** Running total of cash and bank after this movement. */
  balance: number;
}

/** Cash and bank as of `to`; revenue, expenses and profit within [from, to]. */
export interface AccountingSummary {
  from: string;
  to: string;
  cash: number;
  bank: number;
  revenue: number;
  expenses: number;
  profit: number;
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
