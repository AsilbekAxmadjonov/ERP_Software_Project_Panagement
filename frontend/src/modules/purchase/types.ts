export const OrderStatus = { DRAFT: 'DRAFT', CONFIRMED: 'CONFIRMED', RECEIVED: 'RECEIVED', CANCELLED: 'CANCELLED' } as const;
export type OrderStatus = (typeof OrderStatus)[keyof typeof OrderStatus];

export const PaymentProgress = { UNPAID: 'UNPAID', PARTIAL: 'PARTIAL', PAID: 'PAID' } as const;
export type PaymentProgress = (typeof PaymentProgress)[keyof typeof PaymentProgress];

export const PaymentChannel = { CASH: 'CASH', BANK: 'BANK' } as const;
export type PaymentChannel = (typeof PaymentChannel)[keyof typeof PaymentChannel];

export interface PurchaseOrderLine {
  productId: number;
  productName: string;
  unit: string;
  quantity: number;
  unitCost: number;
  lineTotal: number;
}

export interface PurchaseOrder {
  id: number;
  number: string;
  supplierId: number;
  supplierName: string;
  orderDate: string;
  status: OrderStatus;
  receivedDate: string | null;
  total: number;
  /** The total once the goods are received; zero before that. */
  billed: number;
  paid: number;
  outstanding: number;
  paymentStatus: PaymentProgress;
  createdBy: string;
  lines: PurchaseOrderLine[];
}

export interface SupplierPayment {
  id: number;
  supplierId: number;
  supplierName: string;
  orderId: number | null;
  orderNumber: string | null;
  date: string;
  amount: number;
  method: PaymentChannel;
  note: string | null;
  createdBy: string;
}

export interface SupplierBalance {
  partnerId: number;
  name: string;
  phone: string | null;
  balance: number;
}

export interface LedgerRow {
  date: string;
  reference: string | null;
  description: string | null;
  debit: number;
  credit: number;
  balance: number;
}

/** Minimal views of other modules' data, fetched over HTTP so the purchase module has no code dependency on them. */
export interface SupplierOption {
  id: number;
  name: string;
  supplier: boolean;
  active: boolean;
}

export interface ProductOption {
  id: number;
  code: string;
  name: string;
  unit: string;
  averageCost: number;
  quantity: number;
  active: boolean;
}

export interface OrderInput {
  supplierId: number;
  date: string;
  items: { productId: number; quantity: number; unitCost: number }[];
}

export interface SupplierPaymentInput {
  supplierId: number;
  orderId?: number;
  amount: number;
  method: PaymentChannel;
  date: string;
  note: string;
}
