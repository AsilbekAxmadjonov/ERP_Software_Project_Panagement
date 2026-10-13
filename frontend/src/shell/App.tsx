import type { ReactNode } from 'react';
import { Navigate, NavLink, Outlet, Route, Routes } from 'react-router-dom';
import { __ } from '../lib/i18n';
import { route, type RouteName } from '../lib/routes';
import { AccountsPage } from '../modules/accounting/pages/AccountsPage';
import { JournalPage } from '../modules/accounting/pages/JournalPage';
import { LedgerPage } from '../modules/accounting/pages/LedgerPage';
import { TrialBalancePage } from '../modules/accounting/pages/TrialBalancePage';
import { LoginPage } from '../modules/identity/pages/LoginPage';
import { UsersPage } from '../modules/identity/pages/UsersPage';
import { useSession } from '../modules/identity/session';
import { UserRole } from '../modules/identity/types';
import { ProductsPage } from '../modules/inventory/pages/ProductsPage';
import { PartnersPage } from '../modules/partners/pages/PartnersPage';
import { CustomersPage } from '../modules/sales/pages/CustomersPage';
import { InvoicesPage } from '../modules/sales/pages/InvoicesPage';
import { PaymentsPage } from '../modules/sales/pages/PaymentsPage';
import { SalesReportPage } from '../modules/sales/pages/SalesReportPage';

interface PageDefinition {
  name: RouteName;
  label: string;
  roles: readonly UserRole[];
  element: ReactNode;
}

const EVERYONE = [UserRole.CEO, UserRole.MANAGER, UserRole.ACCOUNTANT] as const;

const PAGES: PageDefinition[] = [
  { name: 'sales.invoices', label: __('Invoices'), roles: EVERYONE, element: <InvoicesPage /> },
  { name: 'sales.payments', label: __('Payments'), roles: EVERYONE, element: <PaymentsPage /> },
  { name: 'sales.customers', label: __('Customers'), roles: EVERYONE, element: <CustomersPage /> },
  { name: 'sales.report', label: __('Sales report'), roles: EVERYONE, element: <SalesReportPage /> },
  { name: 'inventory.products', label: __('Stock'), roles: EVERYONE, element: <ProductsPage /> },
  { name: 'partners.index', label: __('Partners'), roles: EVERYONE, element: <PartnersPage /> },
  { name: 'accounting.accounts', label: __('Accounts'), roles: EVERYONE, element: <AccountsPage /> },
  { name: 'accounting.journal', label: __('Journal'), roles: EVERYONE, element: <JournalPage /> },
  { name: 'accounting.ledger', label: __('Ledger'), roles: EVERYONE, element: <LedgerPage /> },
  { name: 'accounting.trial-balance', label: __('Trial balance'), roles: EVERYONE, element: <TrialBalancePage /> },
  { name: 'users.index', label: __('Users'), roles: [UserRole.CEO], element: <UsersPage /> },
];

function Shell() {
  const { user, loading, logout } = useSession();
  if (loading) return <p className="p-6 text-slate-500">{__('Loading…')}</p>;
  if (user === null) return <Navigate to={route('login')} replace />;

  return (
    <div className="min-h-screen">
      <header className="border-b border-slate-200 bg-white px-6 py-3">
        <div className="flex items-center justify-between">
          <span className="text-lg font-semibold text-indigo-700">{__('Kaizen ERP')}</span>
          <div className="flex items-center gap-3 text-sm">
            <span className="text-slate-600">{user.fullName ?? user.username} · {user.role}</span>
            <button className="btn-secondary" onClick={logout}>{__('Sign out')}</button>
          </div>
        </div>
        <nav className="mt-2 flex flex-wrap gap-x-5 gap-y-1">
          {PAGES.filter((page) => page.roles.includes(user.role)).map((page) => (
            <NavLink
              key={page.name}
              to={route(page.name)}
              className={({ isActive }) => (isActive ? 'font-medium text-indigo-700' : 'text-slate-600 hover:text-slate-900')}
            >
              {page.label}
            </NavLink>
          ))}
        </nav>
      </header>
      <main className="mx-auto max-w-6xl p-6">
        <Outlet />
      </main>
    </div>
  );
}

function RequireRole({ roles, children }: { roles: readonly UserRole[]; children: ReactNode }) {
  const { user } = useSession();
  if (user === null || !roles.includes(user.role)) return <p className="text-slate-500">{__('You do not have access to this page.')}</p>;
  return <>{children}</>;
}

export function App() {
  return (
    <Routes>
      <Route path={route('login')} element={<LoginPage />} />
      <Route element={<Shell />}>
        <Route index element={<Navigate to={route('sales.invoices')} replace />} />
        {PAGES.map((page) => (
          <Route key={page.name} path={route(page.name)} element={<RequireRole roles={page.roles}>{page.element}</RequireRole>} />
        ))}
      </Route>
    </Routes>
  );
}
