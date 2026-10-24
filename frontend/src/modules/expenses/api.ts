import { http } from '../../lib/http';
import type { CashEntry, CashEntryInput, Category, CategoryCreateInput, CategoryReport, CategoryUpdateInput, EntryType, LedgerAccount } from './types';

export const expensesApi = {
  categories: () => http.get<Category[]>('/api/expenses/categories'),
  createCategory: (input: CategoryCreateInput) => http.post<Category>('/api/expenses/categories', input),
  updateCategory: (id: number, input: CategoryUpdateInput) => http.put<Category>(`/api/expenses/categories/${id}`, input),
  accounts: (type: EntryType) => http.get<LedgerAccount[]>(`/api/expenses/accounts?type=${type}`),
  entries: (type: EntryType) => http.get<CashEntry[]>(`/api/expenses/entries?type=${type}`),
  entry: (id: number) => http.get<CashEntry>(`/api/expenses/entries/${id}`),
  record: (input: CashEntryInput) => http.post<CashEntry>('/api/expenses/entries', input),
  cancel: (id: number, date: string) => http.post<CashEntry>(`/api/expenses/entries/${id}/cancel`, { date }),
  report: (from: string, to: string) => {
    const query = new URLSearchParams();
    if (from !== '') query.set('from', from);
    if (to !== '') query.set('to', to);
    return http.get<CategoryReport>(`/api/expenses/report?${query.toString()}`);
  },
};
