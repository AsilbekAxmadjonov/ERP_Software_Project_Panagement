import { http } from '../../lib/http';
import type { Employee, EmployeeInput, LedgerEntryInput, LedgerRow, Statement } from './types';

export const employeesApi = {
  list: () => http.get<Employee[]>('/api/employees'),
  get: (id: number) => http.get<Employee>(`/api/employees/${id}`),
  create: (input: EmployeeInput) => http.post<Employee>('/api/employees', input),
  update: (id: number, input: EmployeeInput) => http.put<Employee>(`/api/employees/${id}`, input),
  ledger: (id: number) => http.get<LedgerRow[]>(`/api/employees/${id}/ledger`),
  record: (id: number, input: LedgerEntryInput) => http.post<LedgerRow>(`/api/employees/${id}/ledger`, input),
  statement: (id: number, from: string, to: string) => {
    const query = new URLSearchParams();
    if (from !== '') query.set('from', from);
    if (to !== '') query.set('to', to);
    return http.get<Statement>(`/api/employees/${id}/statement?${query.toString()}`);
  },
  accrueSalaries: (period: string, date: string) => http.post<LedgerRow[]>('/api/employees/payroll/accrue', { period, date }),
};
