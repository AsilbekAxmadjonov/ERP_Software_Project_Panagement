import { Check } from 'lucide-react';
import { useState, type ReactNode } from 'react';

/** The white "paper" a record is shown on. */
export function Sheet({ children, ribbon }: { children: ReactNode; ribbon?: ReactNode }) {
  return (
    <div className="mx-auto max-w-6xl px-2 py-4 sm:px-4">
      <div className="o-sheet relative overflow-hidden rounded border border-gray-200 bg-white px-6 py-6 shadow-sm sm:px-10">
        {ribbon}
        {children}
      </div>
    </div>
  );
}

/** Buttons on the left, workflow stages on the right, like Odoo's form header. */
export function StatusBar<S extends string>({ stages, current, children }: { stages: readonly { key: S; label: string }[]; current?: S; children?: ReactNode }) {
  const currentIndex = stages.findIndex((s) => s.key === current);
  return (
    <div className="no-print border-b border-gray-200 bg-white px-4 py-2">
      <div className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-2">
        <div className="flex flex-wrap items-center gap-2">{children}</div>
        {stages.length > 0 && (
          <ol className="flex overflow-hidden rounded border border-gray-300 text-xs font-medium">
            {stages.map((s, i) => (
              <li
                key={s.key}
                aria-current={s.key === current ? 'step' : undefined}
                className={`flex items-center gap-1 px-3 py-1.5 ${i > 0 ? 'border-l border-gray-300' : ''} ${
                  s.key === current ? 'bg-brand text-white' : i < currentIndex ? 'bg-white text-gray-700' : 'bg-white text-gray-400'
                }`}
              >
                {i < currentIndex && <Check className="h-3 w-3" />}
                {s.label}
              </li>
            ))}
          </ol>
        )}
      </div>
    </div>
  );
}

export function SheetTitle({ overline, children }: { overline?: ReactNode; children: ReactNode }) {
  return (
    <div className="mb-6">
      {overline && <div className="text-sm text-gray-500">{overline}</div>}
      <h1 className="text-3xl font-semibold text-gray-900">{children}</h1>
    </div>
  );
}

export function FieldGroup({ children, title }: { children: ReactNode; title?: string }) {
  return (
    <div>
      {title && <h2 className="mb-2 border-b border-gray-200 pb-1 text-xs font-bold tracking-wide text-gray-700 uppercase">{title}</h2>}
      <div className="grid grid-cols-[minmax(8rem,auto)_1fr] items-center gap-x-4 gap-y-2.5">{children}</div>
    </div>
  );
}

export function Field({ label, htmlFor, children }: { label: string; htmlFor?: string; children: ReactNode }) {
  return (
    <>
      <label htmlFor={htmlFor} className="text-sm font-semibold text-gray-800">{label}</label>
      <div className="min-w-0">{children}</div>
    </>
  );
}

export function Columns({ children }: { children: ReactNode }) {
  return <div className="grid gap-x-12 gap-y-4 md:grid-cols-2">{children}</div>;
}

export interface Tab {
  key: string;
  label: string;
  content: ReactNode;
}

export function Notebook({ tabs }: { tabs: readonly Tab[] }) {
  const [active, setActive] = useState(tabs[0]?.key);
  const current = tabs.find((t) => t.key === active) ?? tabs[0];
  return (
    <div className="mt-8">
      <div role="tablist" className="flex gap-1 border-b border-gray-300">
        {tabs.map((t) => (
          <button
            key={t.key}
            role="tab"
            type="button"
            aria-selected={t.key === current.key}
            onClick={() => setActive(t.key)}
            className={`-mb-px rounded-t border px-4 py-2 text-sm ${t.key === current.key ? 'border-gray-300 border-b-white bg-white font-medium text-gray-900' : 'border-transparent text-accent hover:text-accent-dark'}`}
          >
            {t.label}
          </button>
        ))}
      </div>
      <div role="tabpanel" className="pt-4">{current.content}</div>
    </div>
  );
}

/** Odoo "smart button": an icon, a figure and a caption that link to related records. */
export function SmartButton({ icon, value, label, onClick }: { icon: ReactNode; value: ReactNode; label: string; onClick?: () => void }) {
  return (
    <button type="button" onClick={onClick} className="flex min-w-32 items-center gap-2 border border-gray-200 bg-white px-3 py-1.5 text-left hover:bg-gray-50">
      <span className="text-gray-500">{icon}</span>
      <span className="leading-tight">
        <span className="block text-sm font-semibold text-accent">{value}</span>
        <span className="block text-xs text-gray-600">{label}</span>
      </span>
    </button>
  );
}

export function SmartButtons({ children }: { children: ReactNode }) {
  return <div className="no-print -mx-6 -mt-6 mb-6 flex flex-wrap justify-end border-b border-gray-200 sm:-mx-10">{children}</div>;
}

/** Totals block under order lines, right-aligned like Odoo. */
export function Totals({ rows }: { rows: readonly { label: string; value: string; strong?: boolean }[] }) {
  return (
    <div className="mt-4 flex justify-end">
      <dl className="grid w-72 grid-cols-2 gap-x-6 gap-y-1 text-sm">
        {rows.map((r) => (
          <div key={r.label} className={`contents ${r.strong ? 'text-base font-semibold' : ''}`}>
            <dt className={`text-right ${r.strong ? 'border-t border-gray-300 pt-1' : 'text-gray-600'}`}>{r.label}</dt>
            <dd className={`text-right tabular-nums ${r.strong ? 'border-t border-gray-300 pt-1' : ''}`}>{r.value}</dd>
          </div>
        ))}
      </dl>
    </div>
  );
}
