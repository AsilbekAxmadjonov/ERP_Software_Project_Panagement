import { useCallback, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { date, money } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { route } from '../../../lib/routes';
import { useLoad } from '../../../lib/useLoad';
import { Badge } from '../../../ui/Badge';
import { ControlPanel } from '../../../ui/ControlPanel';
import { StatCard } from '../../../ui/EmptyState';
import { listSheet, type ExportSpec } from '../../../ui/export';
import { ListView, type Column } from '../../../ui/ListView';
import { accountingApi } from '../api';
import { DateRangeFilter, ReportSheet, StatementTable, statementSheet, type StatementSection } from '../components/ReportSheet';
import { ORIGIN, originLabel } from '../labels';
import { PERIOD_PRESETS } from '../periods';
import type { CashFlow, CashFlowOrigin, CashMovement } from '../types';

type Day = CashFlow['days'][number];
type Breakdown = 'day' | 'entry';

const DAY_COLUMNS: readonly Column<Day>[] = [
  { key: 'date', label: __('Date'), render: (d) => date(d.date), sortValue: (d) => d.date },
  { key: 'in', label: __('Cash in'), align: 'right', render: (d) => (d.inflow > 0 ? money(d.inflow) : ''), sum: (d) => d.inflow, formatSum: money },
  { key: 'out', label: __('Cash out'), align: 'right', render: (d) => (d.outflow > 0 ? money(d.outflow) : ''), sum: (d) => d.outflow, formatSum: money },
  { key: 'net', label: __('Net'), align: 'right', render: (d) => money(d.inflow - d.outflow), sum: (d) => d.inflow - d.outflow, formatSum: money },
  { key: 'closing', label: __('Closing balance'), align: 'right', render: (d) => <span className="font-medium">{money(d.closing)}</span>, exportValue: (d) => d.closing },
];

type Movement = CashMovement & { index: number };

const MOVEMENT_COLUMNS: readonly Column<Movement>[] = [
  { key: 'date', label: __('Date'), render: (m) => date(m.date) },
  { key: 'reference', label: __('Reference'), render: (m) => <span className="font-medium">{m.reference ?? `#${m.entryId}`}</span>, exportValue: (m) => m.reference ?? `#${m.entryId}` },
  { key: 'description', label: __('Label'), render: (m) => m.description, className: 'text-gray-600' },
  { key: 'origin', label: __('Origin'), render: (m) => <Badge tone={ORIGIN[m.origin]?.tone ?? 'gray'}>{originLabel(m.origin)}</Badge>, exportValue: (m) => originLabel(m.origin) },
  { key: 'account', label: __('Account'), render: (m) => m.accountName },
  { key: 'in', label: __('Cash in'), align: 'right', render: (m) => (m.inflow > 0 ? money(m.inflow) : ''), sum: (m) => m.inflow, formatSum: money },
  { key: 'out', label: __('Cash out'), align: 'right', render: (m) => (m.outflow > 0 ? money(m.outflow) : ''), sum: (m) => m.outflow, formatSum: money },
  { key: 'balance', label: __('Balance'), align: 'right', render: (m) => <span className="font-medium">{money(m.balance)}</span>, exportValue: (m) => m.balance },
];

const BREAKDOWNS: Record<Breakdown, string> = {
  day: __('By day'),
  entry: __('By entry'),
};

const originRows = (origins: readonly CashFlowOrigin[]) =>
  origins.map((o) => ({ name: `${originLabel(o.origin)} (${o.entries})`, amount: o.amount }));

export function CashFlowPage() {
  const navigate = useNavigate();
  const [range, setRange] = useState(PERIOD_PRESETS.month.range);
  const [breakdown, setBreakdown] = useState<Breakdown>('day');
  const load = useCallback(() => accountingApi.cashFlow(range.from, range.to), [range]);
  const { data, loading, error } = useLoad<CashFlow | null>(load, null);

  const sections: StatementSection[] = data
    ? [
        { title: __('Cash in by origin'), rows: originRows(data.inflowsByOrigin), empty: __('No cash received'), total: { label: __('Total cash in'), amount: data.inflow } },
        { title: __('Cash out by origin'), rows: originRows(data.outflowsByOrigin), empty: __('No cash paid out'), total: { label: __('Total cash out'), amount: data.outflow } },
      ]
    : [];
  const results = data
    ? [
        { label: __('Net change'), amount: data.inflow - data.outflow },
        { label: __('Closing balance'), amount: data.closing },
      ]
    : [];

  const exportData = (): ExportSpec => ({
    fileName: `cash-flow_${data?.from ?? range.from}_${data?.to ?? range.to}`,
    sheets: data
      ? [
          {
            name: __('Accounts'),
            columns: [{ label: __('Code'), format: 'text' }, { label: __('Account') }, { label: __('Opening') }, { label: __('Cash in') }, { label: __('Cash out') }, { label: __('Closing') }],
            rows: [
              ...data.accounts.map((a) => [a.code, a.name, a.opening, a.inflow, a.outflow, a.closing]),
              [null, __('Total'), data.opening, data.inflow, data.outflow, data.closing].map((value) => ({ value, bold: true })),
            ],
          },
          statementSheet(__('By origin'), sections, results),
          listSheet(__('By day'), DAY_COLUMNS, data.days),
          listSheet(__('By entry'), MOVEMENT_COLUMNS, data.movements.map((m, index) => ({ ...m, index }))),
        ]
      : [],
  });

  return (
    <>
      <ControlPanel breadcrumbs={[{ label: __('Cash flow') }]} printTitle={false} exportData={exportData} />
      <div className="space-y-4 p-4">
        <DateRangeFilter value={range} onChange={setRange} />
        {error && <p className="no-print mx-auto max-w-4xl text-red-600">{error}</p>}
        {data && (
          <div className="mx-auto grid max-w-4xl gap-3 sm:grid-cols-4">
            <StatCard label={__('Opening balance')} value={money(data.opening)} />
            <StatCard label={__('Cash in')} value={money(data.inflow)} tone="success" />
            <StatCard label={__('Cash out')} value={money(data.outflow)} tone="danger" />
            <StatCard label={__('Closing balance')} value={money(data.closing)} />
          </div>
        )}
        <ReportSheet
          title={__('Cash flow (Kassa)')}
          subtitle={data ? __('From :from to :to').replace(':from', date(data.from)).replace(':to', date(data.to)) : __('Loading…')}
          loading={loading}
        >
          {data && (
            <>
              <table className="w-full">
                <thead className="border-b border-gray-300">
                  <tr>
                    <th className="th">{__('Account')}</th>
                    <th className="th text-right">{__('Opening')}</th>
                    <th className="th text-right">{__('Cash in')}</th>
                    <th className="th text-right">{__('Cash out')}</th>
                    <th className="th text-right">{__('Closing')}</th>
                  </tr>
                </thead>
                <tbody>
                  {data.accounts.map((a) => (
                    <tr key={a.code} className="border-t border-gray-100">
                      <td className="td pl-6"><span className="mr-2 font-mono text-gray-500">{a.code}</span>{a.name}</td>
                      <td className="td text-right tabular-nums">{money(a.opening)}</td>
                      <td className="td text-right tabular-nums">{money(a.inflow)}</td>
                      <td className="td text-right tabular-nums">{money(a.outflow)}</td>
                      <td className="td text-right tabular-nums">{money(a.closing)}</td>
                    </tr>
                  ))}
                </tbody>
                <tfoot className="border-t-2 border-gray-300 font-semibold">
                  <tr>
                    <td className="td">{__('Total')}</td>
                    <td className="td text-right tabular-nums">{money(data.opening)}</td>
                    <td className="td text-right tabular-nums">{money(data.inflow)}</td>
                    <td className="td text-right tabular-nums">{money(data.outflow)}</td>
                    <td className="td text-right tabular-nums">{money(data.closing)}</td>
                  </tr>
                </tfoot>
              </table>
              <div className="mt-4 border-t border-gray-200">
                <StatementTable sections={sections} results={results} />
              </div>
            </>
          )}
        </ReportSheet>

        {data && (
          <div className="card mx-auto max-w-4xl p-0">
            <div className="flex flex-wrap items-center justify-between gap-2 border-b border-gray-200 px-4 py-2.5">
              <h2 className="font-semibold">{__('Breakdown')}</h2>
              <div className="no-print flex overflow-hidden rounded border border-gray-300 text-sm">
                {(Object.keys(BREAKDOWNS) as Breakdown[]).map((b) => (
                  <button key={b} type="button" aria-pressed={b === breakdown} onClick={() => setBreakdown(b)} className={`px-3 py-1 ${b === breakdown ? 'bg-brand text-white' : 'bg-white hover:bg-gray-50'}`}>
                    {BREAKDOWNS[b]}
                  </button>
                ))}
              </div>
            </div>
            {breakdown === 'day' ? (
              <ListView rows={data.days} columns={DAY_COLUMNS} rowKey={(d) => d.date} loading={loading} />
            ) : (
              <ListView
                rows={data.movements.map((m, index) => ({ ...m, index }))}
                columns={MOVEMENT_COLUMNS}
                rowKey={(m) => m.index}
                loading={loading}
                onOpen={(m) => navigate(route('accounting.journal.show', { id: m.entryId }))}
              />
            )}
          </div>
        )}
      </div>
    </>
  );
}
