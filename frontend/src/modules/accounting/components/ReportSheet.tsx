import { Fragment, type ReactNode } from 'react';
import { money } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { PERIOD_PRESETS, type DateRange, type PeriodPreset } from '../periods';

/** Printable page of a financial report: title and period on top, the statement below. */
export function ReportSheet({ title, subtitle, loading, children }: { title: string; subtitle: string; loading?: boolean; children: ReactNode }) {
  return (
    <div className="o-sheet card mx-auto max-w-4xl p-0">
      <header className="border-b border-gray-200 px-6 py-4">
        <h1 className="text-xl font-semibold text-gray-900">{title}</h1>
        <p className="text-gray-500">{subtitle}</p>
      </header>
      <div className={loading ? 'opacity-50' : ''}>{children}</div>
    </div>
  );
}

export interface StatementRow {
  code?: string;
  name: string;
  amount: number;
}

export interface StatementSection {
  title: string;
  rows: readonly StatementRow[];
  /** Shown when the section has no rows. */
  empty?: string;
  total: { label: string; amount: number };
}

/** Odoo-style statement: section headers, indented account lines, a total per section and bold results at the end. */
export function StatementTable({ sections, results }: { sections: readonly StatementSection[]; results: readonly { label: string; amount: number }[] }) {
  return (
    <table className="w-full">
      <thead className="border-b border-gray-300">
        <tr>
          <th className="th">{__('Account')}</th>
          <th className="th text-right">{__('Balance')}</th>
        </tr>
      </thead>
      <tbody>
        {sections.map((s) => (
          <Fragment key={s.title}>
            <tr className="border-t border-gray-200 bg-gray-50">
              <td className="td font-semibold tracking-wide text-gray-700 uppercase" colSpan={2}>{s.title}</td>
            </tr>
            {s.rows.length === 0 && (
              <tr><td className="td pl-10 text-gray-400 italic" colSpan={2}>{s.empty ?? __('Nothing to report')}</td></tr>
            )}
            {s.rows.map((r) => (
              <tr key={`${r.code ?? ''}-${r.name}`} className="border-t border-gray-100">
                <td className="td pl-10">
                  {r.code && <span className="mr-2 font-mono text-gray-500">{r.code}</span>}
                  {r.name}
                </td>
                <td className={`td text-right tabular-nums ${r.amount < 0 ? 'text-red-600' : ''}`}>{money(r.amount)}</td>
              </tr>
            ))}
            <tr className="border-t border-gray-300 font-semibold">
              <td className="td pl-6">{s.total.label}</td>
              <td className="td text-right tabular-nums">{money(s.total.amount)}</td>
            </tr>
          </Fragment>
        ))}
      </tbody>
      <tfoot>
        {results.map((r) => (
          <tr key={r.label} className="border-t-2 border-gray-400 text-base font-bold">
            <td className="td py-2">{r.label}</td>
            <td className={`td py-2 text-right tabular-nums ${r.amount < 0 ? 'text-red-600' : ''}`}>{money(r.amount)}</td>
          </tr>
        ))}
      </tfoot>
    </table>
  );
}

/** From/to dates with quick period presets; hidden when printing. */
export function DateRangeFilter({ value, onChange }: { value: DateRange; onChange: (range: DateRange) => void }) {
  return (
    <div className="card no-print mx-auto flex max-w-4xl flex-wrap items-end gap-3">
      <div>
        <label className="label" htmlFor="report-from">{__('From')}</label>
        <input id="report-from" type="date" className="input" value={value.from} max={value.to} onChange={(e) => onChange({ ...value, from: e.target.value })} />
      </div>
      <div>
        <label className="label" htmlFor="report-to">{__('To')}</label>
        <input id="report-to" type="date" className="input" value={value.to} min={value.from} onChange={(e) => onChange({ ...value, to: e.target.value })} />
      </div>
      <div className="flex flex-wrap gap-1">
        {(Object.keys(PERIOD_PRESETS) as PeriodPreset[]).map((p) => (
          <button key={p} type="button" className="btn-secondary" onClick={() => onChange(PERIOD_PRESETS[p].range())}>{PERIOD_PRESETS[p].label}</button>
        ))}
      </div>
    </div>
  );
}
