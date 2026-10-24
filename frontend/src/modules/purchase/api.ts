import { http } from '../../lib/http';
import type {
  LedgerRow, OrderInput, ProductOption, PurchaseOrder, PurchaseReportRow, SupplierBalance, SupplierOption, SupplierPayment, SupplierPaymentInput,
} from './types';

export interface ReportFilter {
  from: string;
  to: string;
  supplierId: string;
  productId: string;
}

export const purchaseApi = {
  orders: () => http.get<PurchaseOrder[]>('/api/purchase/orders'),
  order: (id: number) => http.get<PurchaseOrder>(`/api/purchase/orders/${id}`),
  createOrder: (input: OrderInput) => http.post<PurchaseOrder>('/api/purchase/orders', input),
  confirm: (id: number) => http.post<PurchaseOrder>(`/api/purchase/orders/${id}/confirm`),
  receive: (id: number, date: string) => http.post<PurchaseOrder>(`/api/purchase/orders/${id}/receive`, { date }),
  cancel: (id: number) => http.post<PurchaseOrder>(`/api/purchase/orders/${id}/cancel`),
  payments: () => http.get<SupplierPayment[]>('/api/purchase/payments'),
  recordPayment: (input: SupplierPaymentInput) => http.post<SupplierPayment>('/api/purchase/payments', input),
  supplierBalances: () => http.get<SupplierBalance[]>('/api/supplier-balances'),
  supplierLedger: (partnerId: number) => http.get<LedgerRow[]>(`/api/accounting/ledger/partner/${partnerId}`),
  report: (f: ReportFilter) => {
    const query = new URLSearchParams(Object.entries(f).filter(([, value]) => value !== ''));
    return http.get<PurchaseReportRow[]>(`/api/purchase/report?${query.toString()}`);
  },
  supplierOptions: () => http.get<SupplierOption[]>('/api/partners'),
  productOptions: () => http.get<ProductOption[]>('/api/inventory/products'),
};
