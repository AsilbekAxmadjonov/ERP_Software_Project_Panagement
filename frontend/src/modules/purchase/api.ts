import { http } from '../../lib/http';
import type { LedgerRow, OrderInput, ProductOption, PurchaseOrder, SupplierBalance, SupplierOption, SupplierPayment, SupplierPaymentInput } from './types';

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
  supplierOptions: () => http.get<SupplierOption[]>('/api/partners'),
  productOptions: () => http.get<ProductOption[]>('/api/inventory/products'),
};
