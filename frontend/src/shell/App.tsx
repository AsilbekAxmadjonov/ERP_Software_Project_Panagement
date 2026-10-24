import { ChevronDown, Grip, LogOut } from 'lucide-react';
import { useCallback, useEffect, useRef, useState, type ReactNode } from 'react';
import { Link, matchPath, Navigate, NavLink, Outlet, Route, Routes, useLocation } from 'react-router-dom';
import { __ } from '../lib/i18n';
import { route, routePattern, type RouteName } from '../lib/routes';
import { LoginPage } from '../modules/identity/pages/LoginPage';
import { useSession } from '../modules/identity/session';
import type { UserRole } from '../modules/identity/types';
import { Avatar } from '../ui/Kanban';
import { APPS, ROLE_LABEL, type AppDefinition, type MenuItem } from './apps';

const findApp = (pathname: string): AppDefinition | undefined =>
  APPS.find((app) => app.routes.some((r) => matchPath({ path: routePattern(r.name), end: true }, pathname)));

function Shell() {
  const { user, loading } = useSession();
  const { pathname } = useLocation();
  if (loading) return <p className="p-6 text-gray-500">{__('Loading…')}</p>;
  if (user === null) return <Navigate to={route('login')} replace />;

  const currentApp = findApp(pathname);

  return (
    <div className="flex min-h-screen flex-col">
      <header className="no-print sticky top-0 z-20 flex h-12 items-center gap-1 bg-brand px-2 text-white shadow">
        <Link to={route('home')} className="rounded p-2 hover:bg-white/10" title={__('Home menu')} aria-label={__('Home menu')}>
          <Grip className="h-5 w-5" />
        </Link>
        {currentApp ? (
          <>
            <Link to={route(currentApp.routes[0].name)} className="mr-2 px-2 text-base font-semibold">{currentApp.label}</Link>
            <nav className="flex min-w-0 items-center overflow-x-auto">
              {currentApp.menus.filter((m) => canSee(m, user.role)).map((m) => (
                <TopMenu key={m.label} item={m} role={user.role} />
              ))}
            </nav>
          </>
        ) : (
          <span className="px-2 text-base font-semibold">{__('Kaizen ERP')}</span>
        )}
        <div className="ml-auto">
          <UserMenu />
        </div>
      </header>
      <main className="flex-1">
        <Outlet />
      </main>
    </div>
  );
}

const canSee = (item: { roles?: readonly UserRole[] }, role: UserRole) => item.roles === undefined || item.roles.includes(role);

function useDismiss(open: boolean, close: () => void) {
  const ref = useRef<HTMLDivElement>(null);
  const { pathname } = useLocation();
  useEffect(close, [pathname, close]);
  useEffect(() => {
    if (!open) return;
    const onDown = (e: MouseEvent) => ref.current && !ref.current.contains(e.target as Node) && close();
    const onKey = (e: KeyboardEvent) => e.key === 'Escape' && close();
    document.addEventListener('mousedown', onDown);
    document.addEventListener('keydown', onKey);
    return () => {
      document.removeEventListener('mousedown', onDown);
      document.removeEventListener('keydown', onKey);
    };
  }, [open, close]);
  return ref;
}

const menuLinkClass = ({ isActive }: { isActive: boolean }) =>
  `block rounded px-3 py-1.5 text-sm whitespace-nowrap hover:bg-white/10 ${isActive ? 'bg-white/15 font-medium' : ''}`;

function TopMenu({ item, role }: { item: MenuItem; role: UserRole }) {
  const [open, setOpen] = useState(false);
  const ref = useDismiss(open, useCallback(() => setOpen(false), []));
  const { pathname } = useLocation();

  if (item.name) return <NavLink to={route(item.name)} end className={menuLinkClass}>{item.label}</NavLink>;

  const children = (item.children ?? []).filter((c) => canSee(c, role));
  const active = children.some((c) => matchPath({ path: routePattern(c.name), end: false }, pathname));
  return (
    <div ref={ref} className="relative">
      <button type="button" onClick={() => setOpen(!open)} aria-expanded={open} className={`flex items-center gap-1 rounded px-3 py-1.5 text-sm whitespace-nowrap hover:bg-white/10 ${active ? 'bg-white/15 font-medium' : ''}`}>
        {item.label}
        <ChevronDown className="h-3.5 w-3.5 opacity-70" />
      </button>
      {open && (
        <div className="fixed z-30 mt-1 min-w-48 rounded border border-gray-200 bg-white py-1 text-gray-800 shadow-lg">
          {children.map((c) => (
            <NavLink key={c.name} to={route(c.name)} className={({ isActive }) => `block px-4 py-1.5 text-sm hover:bg-gray-100 ${isActive ? 'font-medium text-brand' : ''}`}>
              {c.label}
            </NavLink>
          ))}
        </div>
      )}
    </div>
  );
}

function UserMenu() {
  const { user, logout } = useSession();
  const [open, setOpen] = useState(false);
  const ref = useDismiss(open, useCallback(() => setOpen(false), []));
  if (user === null) return null;
  const name = user.fullName ?? user.username;

  return (
    <div ref={ref} className="relative">
      <button type="button" onClick={() => setOpen(!open)} aria-expanded={open} className="flex items-center gap-2 rounded px-2 py-1 hover:bg-white/10">
        <Avatar name={name} size="sm" />
        <span className="hidden text-sm sm:inline">{name}</span>
      </button>
      {open && (
        <div className="absolute right-0 z-30 mt-1 w-56 rounded border border-gray-200 bg-white py-1 text-gray-800 shadow-lg">
          <div className="border-b border-gray-100 px-4 py-2">
            <div className="font-medium">{name}</div>
            <div className="text-xs text-gray-500">@{user.username} · {ROLE_LABEL[user.role]}</div>
          </div>
          <button type="button" onClick={logout} className="flex w-full items-center gap-2 px-4 py-2 text-left text-sm hover:bg-gray-100">
            <LogOut className="h-4 w-4" />
            {__('Log out')}
          </button>
        </div>
      )}
    </div>
  );
}

function HomeMenu() {
  const { user } = useSession();
  const apps = APPS.filter((app) => user !== null && app.roles.includes(user.role));

  return (
    <div className="min-h-[calc(100vh-3rem)] bg-gradient-to-br from-[#f6f1f5] via-white to-[#e6f3f3] px-6 py-12">
      <div className="mx-auto grid max-w-3xl grid-cols-3 gap-x-4 gap-y-8 sm:grid-cols-4 md:grid-cols-6">
        {apps.map((app) => (
          <Link key={app.key} to={route(app.routes[0].name)} className="group flex flex-col items-center gap-2 text-center" title={app.description}>
            <span className={`flex h-16 w-16 items-center justify-center rounded-2xl text-white shadow-md transition group-hover:-translate-y-0.5 group-hover:shadow-lg ${app.color}`}>
              <app.icon className="h-8 w-8" strokeWidth={1.75} />
            </span>
            <span className="text-sm font-medium text-gray-800">{app.label}</span>
          </Link>
        ))}
      </div>
    </div>
  );
}

function RequireRole({ roles, children }: { roles: readonly UserRole[]; children: ReactNode }) {
  const { user } = useSession();
  if (user === null || !roles.includes(user.role)) return <p className="p-6 text-gray-500">{__('You do not have access to this page.')}</p>;
  return <>{children}</>;
}

const routeRoles = (app: AppDefinition, r: { roles?: readonly UserRole[] }) => r.roles ?? app.roles;

export function App() {
  return (
    <Routes>
      <Route path={routePattern('login')} element={<LoginPage />} />
      <Route element={<Shell />}>
        <Route index element={<HomeMenu />} />
        {APPS.flatMap((app) =>
          app.routes.map((r) => (
            <Route key={r.name} path={routePattern(r.name as RouteName)} element={<RequireRole roles={routeRoles(app, r)}>{r.element}</RequireRole>} />
          )),
        )}
        <Route path="*" element={<p className="p-6 text-gray-500">{__('Page not found.')}</p>} />
      </Route>
    </Routes>
  );
}
