import { http } from '../../lib/http';
import type { Account, AccountKind, EntryInput, JournalEntry, LedgerRow, TrialBalance } from './types';

export const accountingApi = {
  accounts: () => http.get<Account[]>('/api/accounting/accounts'),
  createAccount: (input: { code: string; name: string; type: AccountKind }) => http.post<Account>('/api/accounting/accounts', input),
  setAccountActive: (code: string, active: boolean) => http.put<Account>(`/api/accounting/accounts/${code}`, { active }),
  entries: () => http.get<JournalEntry[]>('/api/accounting/entries'),
  postEntry: (input: EntryInput) => http.post<JournalEntry>('/api/accounting/entries', input),
  reverseEntry: (id: number) => http.post<JournalEntry>(`/api/accounting/entries/${id}/reverse`),
  trialBalance: () => http.get<TrialBalance>('/api/accounting/reports/trial-balance'),
  accountLedger: (code: string) => http.get<LedgerRow[]>(`/api/accounting/ledger/account/${code}`),
};
