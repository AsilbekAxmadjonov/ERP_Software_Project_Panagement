import { Printer } from 'lucide-react';
import { useCallback, useState } from 'react';
import { date, money, today } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { useLoad } from '../../../lib/useLoad';
import { ControlPanel } from '../../../ui/ControlPanel';
import { StatCard } from '../../../ui/EmptyState';
import { ListView, type Column } from '../../../ui/ListView';
import { expensesApi } from '../api';
import type { CategoryReport, CategoryTotal } from '../types';

const COLUMNS: readonly Column<CategoryTotal>[] = [
  { key: 'category', label: __('Category'), render: (r) => <span className="font-medium">{r.categoryName}</span>, sortValue: (r) => r.categoryName },
  { key: 'account', label: __('Ledger account'), render: (r) => r.accountCode, className: 'text-gray-500' },
  { key: 'count', label: __('Entries'), align: 'right', render: (r) => r.count, sortValue: (r) => r.count, sum: (r) => r.count },
  { key: 'total', label: __('Total'), align: 'right', render: (r) => money(r.total), sortValue: (r) => r.total, sum: (r) => r.total, formatSum: money },
];

const startOfMonth = () => `${today().slice(0, 8)}01`;

export function ExpenseReportPage() {
  const [from, setFrom] = useState(startOfMonth());
  const [to, setTo] = useState(today());
  const load = useCallback(() => expensesApi.report(from, to), [from, to]);
  const { data: report, loading, error } = useLoad<CategoryReport | null>(load, null);

  const period = [from && date(from), to && date(to)].filter(Boolean).join(' – ') || __('All dates');

  return (
    <>
      <ControlPanel
        breadcrumbs={[{ label: __('Expenses and income by category') }]}
        actions={<button className="btn-secondary" onClick={() => window.print()}><Printer className="h-4 w-4" />{__('Print')}</button>}
      />
      <div className="mx-auto max-w-7xl space-y-4 p-4">
        <div className="no-print card grid gap-3 sm:grid-cols-4">
          <div>
            <label className="label" htmlFor="er-from">{__('From')}</label>
            <input id="er-from" type="date" className="input" value={from} onChange={(e) => setFrom(e.target.value)} />
          </div>
          <div>
            <label className="label" htmlFor="er-to">{__('To')}</label>
            <input id="er-to" type="date" className="input" value={to} onChange={(e) => setTo(e.target.value)} />
          </div>
        </div>
        <h1 className="text-lg font-semibold">{__('Period')}: {period}</h1>
        {error !== null && <p className="text-red-600" role="alert">{error}</p>}

        <div className="grid gap-3 sm:grid-cols-3">
          <StatCard label={__('Expenses')} value={money(report?.totalExpenses ?? 0)} tone="danger" />
          <StatCard label={__('Other income')} value={money(report?.totalIncome ?? 0)} tone="success" />
          <StatCard label={__('Net')} value={money(report?.net ?? 0)} tone={report !== null && report.net < 0 ? 'danger' : 'default'} hint={__('Other income minus expenses')} />
        </div>

        <section className="card">
          <h2 className="mb-3 font-semibold">{__('Expenses by category')}</h2>
          <ListView rows={report?.expenses ?? []} columns={COLUMNS} rowKey={(r) => r.categoryId} loading={loading} />
        </section>
        <section className="card">
          <h2 className="mb-3 font-semibold">{__('Other income by category')}</h2>
          <ListView rows={report?.income ?? []} columns={COLUMNS} rowKey={(r) => r.categoryId} loading={loading} />
        </section>
      </div>
    </>
  );
}
