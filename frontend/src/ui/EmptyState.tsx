import type { ReactNode } from 'react';

/** Odoo's friendly "nothing here yet" helper shown on an empty list. */
export function EmptyState({ title, hint, action }: { title: string; hint?: string; action?: ReactNode }) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 px-6 py-20 text-center">
      <svg viewBox="0 0 64 64" className="h-20 w-20 text-gray-300" aria-hidden="true">
        <rect x="10" y="8" width="44" height="50" rx="4" fill="none" stroke="currentColor" strokeWidth="3" />
        <path d="M20 22h24M20 32h24M20 42h14" stroke="currentColor" strokeWidth="3" strokeLinecap="round" />
      </svg>
      <h2 className="text-lg font-semibold text-gray-800">{title}</h2>
      {hint && <p className="max-w-md text-gray-500">{hint}</p>}
      {action}
    </div>
  );
}

/** Page wrapper for views that are not a list or a form (dashboards, reports). */
export function Page({ children }: { children: ReactNode }) {
  return <div className="mx-auto max-w-7xl space-y-4 p-4">{children}</div>;
}

export function StatCard({ label, value, tone = 'default', hint, onClick }: { label: string; value: ReactNode; tone?: 'default' | 'warning' | 'danger' | 'success'; hint?: string; onClick?: () => void }) {
  const color = { default: 'text-gray-900', warning: 'text-amber-600', danger: 'text-red-600', success: 'text-emerald-700' }[tone];
  const Tag = onClick ? 'button' : 'div';
  return (
    <Tag onClick={onClick} className={`card text-left ${onClick ? 'transition hover:border-gray-300 hover:shadow-md' : ''}`}>
      <div className="text-xs font-semibold tracking-wide text-gray-500 uppercase">{label}</div>
      <div className={`mt-1 text-2xl font-semibold tabular-nums ${color}`}>{value}</div>
      {hint && <div className="mt-1 text-xs text-gray-500">{hint}</div>}
    </Tag>
  );
}
