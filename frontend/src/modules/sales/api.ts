import { http } from '../../lib/http';
import type {
  CreditNote, CreditNoteInput, CustomerBalance, CustomerOption, Invoice, InvoiceInput, LedgerRow, Payment, PaymentInput, ProductOption, SalesReportRow,
} from './types';

export interface ReportFilter {
  from: string;
  to: string;
  customerId: string;
  productId: string;
}

export const salesApi = {
  invoices: () => http.get<Invoice[]>('/api/sales/invoices'),
  invoice: (id: number) => http.get<Invoice>(`/api/sales/invoices/${id}`),
  issueInvoice: (input: InvoiceInput) => http.post<Invoice>('/api/sales/invoices', input),
  creditNotes: () => http.get<CreditNote[]>('/api/sales/credit-notes'),
  issueCreditNote: (invoiceId: number, input: CreditNoteInput) => http.post<CreditNote>(`/api/sales/invoices/${invoiceId}/credit-notes`, input),
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
