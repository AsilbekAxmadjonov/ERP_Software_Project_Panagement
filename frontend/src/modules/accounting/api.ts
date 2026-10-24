import { http } from '../../lib/http';
import type {
  Account, AccountingSummary, AccountKind, BalanceSheet, CashFlow, EntryInput, JournalEntry, LedgerRow, PartnerOption, PeriodLock, ProfitAndLoss, TrialBalance,
} from './types';

/** Query string from the non-empty values; an empty value lets the backend pick its default. */
const query = (params: Record<string, string>) => new URLSearchParams(Object.entries(params).filter(([, value]) => value !== '')).toString();

export const accountingApi = {
  accounts: () => http.get<Account[]>('/api/accounting/accounts'),
  createAccount: (input: { code: string; name: string; type: AccountKind }) => http.post<Account>('/api/accounting/accounts', input),
  setAccountActive: (code: string, active: boolean) => http.put<Account>(`/api/accounting/accounts/${code}`, { active }),
  entries: () => http.get<JournalEntry[]>('/api/accounting/entries'),
  postEntry: (input: EntryInput) => http.post<JournalEntry>('/api/accounting/entries', input),
  reverseEntry: (id: number) => http.post<JournalEntry>(`/api/accounting/entries/${id}/reverse`),
  trialBalance: () => http.get<TrialBalance>('/api/accounting/reports/trial-balance'),
  profitAndLoss: (from: string, to: string) => http.get<ProfitAndLoss>(`/api/accounting/reports/profit-and-loss?${query({ from, to })}`),
  balanceSheet: (asOf: string) => http.get<BalanceSheet>(`/api/accounting/reports/balance-sheet?${query({ asOf })}`),
  cashFlow: (from: string, to: string) => http.get<CashFlow>(`/api/accounting/reports/cash-flow?${query({ from, to })}`),
  summary: (from: string, to: string) => http.get<AccountingSummary>(`/api/accounting/reports/summary?${query({ from, to })}`),
  accountLedger: (code: string) => http.get<LedgerRow[]>(`/api/accounting/ledger/account/${code}`),
  partnerLedger: (partnerId: number) => http.get<LedgerRow[]>(`/api/accounting/ledger/partner/${partnerId}`),
  partnerOptions: () => http.get<PartnerOption[]>('/api/partners'),
  periodLock: () => http.get<PeriodLock>('/api/accounting/periods'),
  closePeriod: (lockDate: string) => http.post<PeriodLock>('/api/accounting/periods/close', { lockDate }),
  reopenPeriod: (lockDate: string | null) => http.post<PeriodLock>('/api/accounting/periods/reopen', { lockDate }),
};
