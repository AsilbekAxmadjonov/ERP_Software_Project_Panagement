import { http } from '../../lib/http';
import type {
  CustomerBalance, CustomerOption, Invoice, InvoiceInput, LedgerRow, Payment, PaymentInput, ProductOption, SalesReportRow,
} from './types';

export interface ReportFilter {
  from: string;
  to: string;
  customerId: string;
  productId: string;
}

export const salesApi = {
  invoices: () => http.get<Invoice[]>('/api/sales/invoices'),
  issueInvoice: (input: InvoiceInput) => http.post<Invoice>('/api/sales/invoices', input),
  payments: () => http.get<Payment[]>('/api/sales/payments'),
  recordPayment: (input: PaymentInput) => http.post<Payment>('/api/sales/payments', input),
  customerBalances: () => http.get<CustomerBalance[]>('/api/customer-balances'),
  customerLedger: (partnerId: number) => http.get<LedgerRow[]>(`/api/accounting/ledger/partner/${partnerId}`),
  report: (f: ReportFilter) => {
    const query = new URLSearchParams(Object.entries(f).filter(([, value]) => value !== ''));
    return http.get<SalesReportRow[]>(`/api/sales/report?${query.toString()}`);
  },
  customerOptions: () => http.get<CustomerOption[]>('/api/partners'),
  productOptions: () => http.get<ProductOption[]>('/api/inventory/products'),
};
