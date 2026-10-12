import type { ReactNode } from 'react';
import { Navigate, NavLink, Outlet, Route, Routes } from 'react-router-dom';
import { __ } from '../lib/i18n';
import { route, type RouteName } from '../lib/routes';
import { LoginPage } from '../modules/identity/pages/LoginPage';
import { UsersPage } from '../modules/identity/pages/UsersPage';
import { UserRole } from '../modules/identity/types';
import { PartnersPage } from '../modules/partners/pages/PartnersPage';
import { useSession } from '../modules/identity/session';

interface NavItem {
  to: RouteName;
  label: string;
  roles: readonly UserRole[];
}

const ALL_ROLES = [UserRole.CEO, UserRole.MANAGER, UserRole.ACCOUNTANT] as const;

const NAV: NavItem[] = [
  { to: 'partners.index', label: __('Partners'), roles: ALL_ROLES },
  { to: 'users.index', label: __('Users'), roles: [UserRole.CEO] },
];

function Shell() {
  const { user, loading, logout } = useSession();
  if (loading) return <p className="p-6 text-slate-500">{__('Loading…')}</p>;
  if (user === null) return <Navigate to={route('login')} replace />;

  return (
    <div className="min-h-screen">
      <header className="flex items-center justify-between border-b border-slate-200 bg-white px-6 py-3">
        <div className="flex items-center gap-6">
          <span className="text-lg font-semibold text-indigo-700">{__('Kaizen ERP')}</span>
          <nav className="flex gap-4">
            {NAV.filter((item) => item.roles.includes(user.role)).map((item) => (
              <NavLink
                key={item.to}
                to={route(item.to)}
                className={({ isActive }) => (isActive ? 'font-medium text-indigo-700' : 'text-slate-600 hover:text-slate-900')}
              >
                {item.label}
              </NavLink>
            ))}
          </nav>
        </div>
        <div className="flex items-center gap-3 text-sm">
          <span className="text-slate-600">
            {user.fullName ?? user.username} · {user.role}
          </span>
          <button className="btn-secondary" onClick={logout}>
            {__('Sign out')}
          </button>
        </div>
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
        <Route index element={<Navigate to={route('partners.index')} replace />} />
        <Route path={route('partners.index')} element={<PartnersPage />} />
        <Route
          path={route('users.index')}
          element={
            <RequireRole roles={[UserRole.CEO]}>
              <UsersPage />
            </RequireRole>
          }
        />
      </Route>
    </Routes>
  );
}
