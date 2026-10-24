export const EmployeeState = { ACTIVE: 'ACTIVE', INACTIVE: 'INACTIVE' } as const;
export type EmployeeState = (typeof EmployeeState)[keyof typeof EmployeeState];

export const LedgerEntryKind = {
  SALARY_ACCRUED: 'SALARY_ACCRUED',
  BONUS: 'BONUS',
  PAYMENT: 'PAYMENT',
  ADVANCE: 'ADVANCE',
  DEDUCTION: 'DEDUCTION',
} as const;
export type LedgerEntryKind = (typeof LedgerEntryKind)[keyof typeof LedgerEntryKind];

export const PaymentChannel = { CASH: 'CASH', BANK: 'BANK' } as const;
export type PaymentChannel = (typeof PaymentChannel)[keyof typeof PaymentChannel];

export interface Employee {
  id: number;
  fullName: string;
  position: string | null;
  phone: string | null;
  monthlySalary: number;
  status: EmployeeState;
  /** What the company owes the employee; negative when the employee owes an unearned advance. */
  balance: number;
}

export interface LedgerRow {
  id: number;
  reference: string;
  employeeId: number;
  date: string;
  type: LedgerEntryKind;
  /** yyyy-mm, salary accruals only. */
  period: string | null;
  method: PaymentChannel | null;
  description: string | null;
  earned: number;
  settled: number;
  balance: number;
  createdBy: string;
}

export interface Statement {
  employee: Employee;
  from: string | null;
  to: string | null;
  opening: number;
  rows: LedgerRow[];
  totalEarned: number;
  totalSettled: number;
  closing: number;
}

export interface EmployeeInput {
  fullName: string;
  position: string;
  phone: string;
  monthlySalary: number;
  status: EmployeeState;
}

export interface LedgerEntryInput {
  type: LedgerEntryKind;
  date: string;
  amount: number;
  method?: PaymentChannel;
  period?: string;
  description: string;
}
