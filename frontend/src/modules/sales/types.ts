export const PaymentProgress = { UNPAID: 'UNPAID', PARTIAL: 'PARTIAL', PAID: 'PAID', CREDITED: 'CREDITED' } as const;
export type PaymentProgress = (typeof PaymentProgress)[keyof typeof PaymentProgress];

export const PaymentChannel = { CASH: 'CASH', BANK: 'BANK' } as const;
export type PaymentChannel = (typeof PaymentChannel)[keyof typeof PaymentChannel];

export interface InvoiceLine {
  productId: number;
  productName: string;
  unit: string;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
  returnedQuantity: number;
  /** Quantity that can still be returned on a credit note. */
  returnable: number;
}

export interface Invoice {
  id: number;
  number: string;
  customerId: number;
  customerName: string;
  invoiceDate: string;
  total: number;
  cost: number;
  paid: number;
  /** Value of goods returned through credit notes. */
  credited: number;
  outstanding: number;
  status: PaymentProgress;
  createdBy: string;
  lines: InvoiceLine[];
}

export interface Payment {
  id: number;
  customerId: number;
  customerName: string;
  invoiceNumber: string | null;
  date: string;
  amount: number;
  method: PaymentChannel;
  note: string | null;
}

export interface CustomerBalance {
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

export interface SalesReportRow {
  date: string;
  invoice: string;
  customerId: number;
  customer: string;
  productId: number;
  product: string;
  unit: string;
  quantity: number;
  unitPrice: number;
  total: number;
}

/** Minimal views of other modules' data, fetched over HTTP so the sales module has no code dependency on them. */
export interface CustomerOption {
  id: number;
  name: string;
  customer: boolean;
  active: boolean;
}

export interface ProductOption {
  id: number;
  code: string;
  name: string;
  unit: string;
  salePrice: number;
  quantity: number;
  active: boolean;
}

export interface InvoiceInput {
  customerId: number;
  date: string;
  items: { productId: number; quantity: number; unitPrice?: number }[];
}

export interface PaymentInput {
  customerId: number;
  invoiceId?: number;
  amount: number;
  method: PaymentChannel;
  date: string;
  note: string;
}

export interface CreditNoteLine {
  productId: number;
  productName: string;
  unit: string;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
}

export interface CreditNote {
  id: number;
  number: string;
  invoiceId: number;
  invoiceNumber: string;
  customerId: number;
  customerName: string;
  date: string;
  reason: string | null;
  total: number;
  cost: number;
  createdBy: string;
  lines: CreditNoteLine[];
}

export interface CreditNoteInput {
  date: string;
  reason: string;
  items: { productId: number; quantity: number }[];
}
