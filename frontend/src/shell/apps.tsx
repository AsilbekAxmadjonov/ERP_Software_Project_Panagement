import { BookOpenCheck, Boxes, ContactRound, Settings, ShoppingCart, TrendingUp, Wallet, type LucideIcon } from 'lucide-react';
import type { ReactNode } from 'react';
import { __ } from '../lib/i18n';
import type { RouteName } from '../lib/routes';
import { AccountingDashboardPage } from '../modules/accounting/pages/AccountingDashboardPage';
import { AccountsPage } from '../modules/accounting/pages/AccountsPage';
import { JournalEntryFormPage } from '../modules/accounting/pages/JournalEntryFormPage';
import { JournalListPage } from '../modules/accounting/pages/JournalListPage';
import { LedgerPage } from '../modules/accounting/pages/LedgerPage';
import { LockDatesPage } from '../modules/accounting/pages/LockDatesPage';
import { PartnerLedgerPage } from '../modules/accounting/pages/PartnerLedgerPage';
import { TrialBalancePage } from '../modules/accounting/pages/TrialBalancePage';
import { AuditLogPage } from '../modules/audit/pages/AuditLogPage';
import { CashEntryFormPage } from '../modules/expenses/pages/CashEntryFormPage';
import { ExpensesPage, OtherIncomePage } from '../modules/expenses/pages/CashEntriesPage';
import { CategoriesPage } from '../modules/expenses/pages/CategoriesPage';
import { ExpenseReportPage } from '../modules/expenses/pages/ExpenseReportPage';
import { UsersPage } from '../modules/identity/pages/UsersPage';
import { UserRole } from '../modules/identity/types';
import { ProductFormPage } from '../modules/inventory/pages/ProductFormPage';
import { ProductsPage } from '../modules/inventory/pages/ProductsPage';
import { StockMovesPage } from '../modules/inventory/pages/StockMovesPage';
import { ContactFormPage } from '../modules/partners/pages/ContactFormPage';
import { ContactsPage } from '../modules/partners/pages/ContactsPage';
import { PurchaseOrderFormPage } from '../modules/purchase/pages/PurchaseOrderFormPage';
import { PurchaseOrdersPage } from '../modules/purchase/pages/PurchaseOrdersPage';
import { SupplierPaymentsPage } from '../modules/purchase/pages/SupplierPaymentsPage';
import { VendorsPage } from '../modules/purchase/pages/VendorsPage';
import { CreditNotesPage } from '../modules/sales/pages/CreditNotesPage';
import { CustomersPage } from '../modules/sales/pages/CustomersPage';
import { InvoiceFormPage } from '../modules/sales/pages/InvoiceFormPage';
import { InvoicesPage } from '../modules/sales/pages/InvoicesPage';
import { PaymentsPage } from '../modules/sales/pages/PaymentsPage';
import { SalesReportPage } from '../modules/sales/pages/SalesReportPage';

export const ROLE_LABEL: Record<UserRole, string> = {
  [UserRole.CEO]: __('CEO'),
  [UserRole.MANAGER]: __('Manager'),
  [UserRole.ACCOUNTANT]: __('Accountant'),
};

interface Restricted {
  /** Narrows the app's roles for this entry. */
  roles?: readonly UserRole[];
}

export interface MenuLink extends Restricted {
  label: string;
  name: RouteName;
}

/** A top-bar entry: either a direct link or a dropdown of links. */
export type MenuItem = (MenuLink & { children?: undefined }) | (Restricted & { label: string; name?: undefined; children: readonly MenuLink[] });

export interface AppRoute extends Restricted {
  name: RouteName;
  element: ReactNode;
}

export interface AppDefinition {
  key: string;
  label: string;
  description: string;
  icon: LucideIcon;
  color: string;
  roles: readonly UserRole[];
  menus: readonly MenuItem[];
  /** The first route is the app's landing page. */
  routes: readonly AppRoute[];
}

const EVERYONE = [UserRole.CEO, UserRole.MANAGER, UserRole.ACCOUNTANT] as const;
const FINANCE = [UserRole.CEO, UserRole.ACCOUNTANT] as const;

export const APPS: readonly AppDefinition[] = [
  {
    key: 'sales',
    label: __('Sales'),
    description: __('Invoices, credit notes, payments and customers'),
    icon: TrendingUp,
    color: 'bg-gradient-to-br from-emerald-500 to-teal-700',
    roles: EVERYONE,
    menus: [
      {
        label: __('Orders'),
        children: [
          { label: __('Invoices'), name: 'sales.invoices' },
          { label: __('Credit notes'), name: 'sales.credit-notes' },
          { label: __('Payments'), name: 'sales.payments' },
        ],
      },
      { label: __('Customers'), name: 'sales.customers' },
      { label: __('Reporting'), name: 'sales.report' },
    ],
    routes: [
      { name: 'sales.invoices', element: <InvoicesPage /> },
      { name: 'sales.invoices.new', element: <InvoiceFormPage /> },
      { name: 'sales.invoices.show', element: <InvoiceFormPage /> },
      { name: 'sales.credit-notes', element: <CreditNotesPage /> },
      { name: 'sales.payments', element: <PaymentsPage /> },
      { name: 'sales.customers', element: <CustomersPage /> },
      { name: 'sales.report', element: <SalesReportPage /> },
    ],
  },
  {
    key: 'purchase',
    label: __('Purchase'),
    description: __('Purchase orders, receipts and vendor payments'),
    icon: ShoppingCart,
    color: 'bg-gradient-to-br from-rose-500 to-pink-700',
    roles: EVERYONE,
    menus: [
      {
        label: __('Orders'),
        children: [
          { label: __('Purchase orders'), name: 'purchase.orders' },
          { label: __('Vendor payments'), name: 'purchase.payments' },
        ],
      },
      { label: __('Vendors'), name: 'purchase.vendors' },
    ],
    routes: [
      { name: 'purchase.orders', element: <PurchaseOrdersPage /> },
      { name: 'purchase.orders.new', element: <PurchaseOrderFormPage /> },
      { name: 'purchase.orders.show', element: <PurchaseOrderFormPage /> },
      { name: 'purchase.payments', element: <SupplierPaymentsPage /> },
      { name: 'purchase.vendors', element: <VendorsPage /> },
    ],
  },
  {
    key: 'expenses',
    label: __('Expenses'),
    description: __('Running costs, other income and their categories'),
    icon: Wallet,
    color: 'bg-gradient-to-br from-lime-500 to-green-700',
    roles: EVERYONE,
    menus: [
      { label: __('Expenses'), name: 'expenses.expenses' },
      { label: __('Other income'), name: 'expenses.income' },
      { label: __('Reporting'), children: [{ label: __('By category'), name: 'expenses.report' }] },
      { label: __('Configuration'), children: [{ label: __('Categories'), name: 'expenses.categories' }] },
    ],
    routes: [
      { name: 'expenses.expenses', element: <ExpensesPage /> },
      { name: 'expenses.income', element: <OtherIncomePage /> },
      { name: 'expenses.entries.show', element: <CashEntryFormPage /> },
      { name: 'expenses.categories', element: <CategoriesPage /> },
      { name: 'expenses.report', element: <ExpenseReportPage /> },
    ],
  },
  {
    key: 'accounting',
    label: __('Accounting'),
    description: __('Journal entries, ledgers and reports'),
    icon: BookOpenCheck,
    color: 'bg-gradient-to-br from-violet-500 to-[#714b67]',
    roles: EVERYONE,
    menus: [
      { label: __('Dashboard'), name: 'accounting.dashboard' },
      { label: __('Accounting'), children: [{ label: __('Journal entries'), name: 'accounting.journal' }] },
      {
        label: __('Reporting'),
        children: [
          { label: __('Trial balance'), name: 'accounting.trial-balance' },
          { label: __('General ledger'), name: 'accounting.ledger' },
          { label: __('Partner ledger'), name: 'accounting.partner-ledger' },
        ],
      },
      {
        label: __('Configuration'),
        children: [
          { label: __('Chart of accounts'), name: 'accounting.accounts' },
          { label: __('Lock dates'), name: 'accounting.lock-dates', roles: FINANCE },
        ],
      },
    ],
    routes: [
      { name: 'accounting.dashboard', element: <AccountingDashboardPage /> },
      { name: 'accounting.journal', element: <JournalListPage /> },
      { name: 'accounting.journal.new', element: <JournalEntryFormPage />, roles: FINANCE },
      { name: 'accounting.journal.show', element: <JournalEntryFormPage /> },
      { name: 'accounting.trial-balance', element: <TrialBalancePage /> },
      { name: 'accounting.ledger', element: <LedgerPage /> },
      { name: 'accounting.partner-ledger', element: <PartnerLedgerPage /> },
      { name: 'accounting.accounts', element: <AccountsPage /> },
      { name: 'accounting.lock-dates', element: <LockDatesPage />, roles: FINANCE },
    ],
  },
  {
    key: 'inventory',
    label: __('Inventory'),
    description: __('Products, stock levels and moves'),
    icon: Boxes,
    color: 'bg-gradient-to-br from-amber-400 to-orange-600',
    roles: EVERYONE,
    menus: [
      { label: __('Products'), name: 'inventory.products' },
      { label: __('Reporting'), children: [{ label: __('Moves history'), name: 'inventory.moves' }] },
    ],
    routes: [
      { name: 'inventory.products', element: <ProductsPage /> },
      { name: 'inventory.products.new', element: <ProductFormPage /> },
      { name: 'inventory.products.show', element: <ProductFormPage /> },
      { name: 'inventory.moves', element: <StockMovesPage /> },
    ],
  },
  {
    key: 'contacts',
    label: __('Contacts'),
    description: __('Customers and suppliers directory'),
    icon: ContactRound,
    color: 'bg-gradient-to-br from-sky-500 to-blue-700',
    roles: EVERYONE,
    menus: [{ label: __('Contacts'), name: 'partners.index' }],
    routes: [
      { name: 'partners.index', element: <ContactsPage /> },
      { name: 'partners.new', element: <ContactFormPage /> },
      { name: 'partners.show', element: <ContactFormPage /> },
    ],
  },
  {
    key: 'settings',
    label: __('Settings'),
    description: __('Users, access rights and audit log'),
    icon: Settings,
    color: 'bg-gradient-to-br from-slate-500 to-slate-700',
    roles: [UserRole.CEO],
    menus: [
      { label: __('Users'), name: 'users.index' },
      { label: __('Audit log'), name: 'audit.index' },
    ],
    routes: [
      { name: 'users.index', element: <UsersPage /> },
      { name: 'audit.index', element: <AuditLogPage /> },
    ],
  },
];
