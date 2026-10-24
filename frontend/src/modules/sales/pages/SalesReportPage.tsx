import { useCallback, useState } from 'react';
import { date, money, quantity } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { useLoad } from '../../../lib/useLoad';
import { ControlPanel } from '../../../ui/ControlPanel';
import { StatCard } from '../../../ui/EmptyState';
import { listSheet } from '../../../ui/export';
import { ListView, type Column } from '../../../ui/ListView';
import { Many2One } from '../../../ui/Many2One';
import { salesApi, type ReportFilter } from '../api';
import type { SalesReportRow } from '../types';

const NO_FILTER: ReportFilter = { from: '', to: '', customerId: '', productId: '' };

type Dimension = 'customer' | 'product' | 'month';

const DIMENSIONS: Record<Dimension, { label: string; value: (r: SalesReportRow) => string }> = {
  customer: { label: __('Customer'), value: (r) => r.customer },
  product: { label: __('Product'), value: (r) => r.product },
  month: { label: __('Month'), value: (r) => r.date.slice(0, 7) },
};

const COLUMNS: readonly Column<SalesReportRow>[] = [
  { key: 'date', label: __('Date'), render: (r) => date(r.date), sortValue: (r) => r.date },
  { key: 'invoice', label: __('Invoice'), render: (r) => r.invoice, sortValue: (r) => r.invoice },
  { key: 'customer', label: __('Customer'), render: (r) => r.customer, sortValue: (r) => r.customer },
  { key: 'product', label: __('Product'), render: (r) => r.product, sortValue: (r) => r.product },
  { key: 'qty', label: __('Quantity'), align: 'right', render: (r) => `${quantity(r.quantity)} ${r.unit.toLowerCase()}`, sortValue: (r) => r.quantity, exportFormat: 'quantity' },  { key: 'price', label: __('Unit price'), align: 'right', render: (r) => money(r.unitPrice), exportValue: (r) => r.unitPrice },
  { key: 'total', label: __('Total'), align: 'right', render: (r) => money(r.total), sortValue: (r) => r.total, sum: (r) => r.total, formatSum: money },
];

export function SalesReportPage() {
  const [filter, setFilter] = useState<ReportFilter>(NO_FILTER);
  const [dimension, setDimension] = useState<Dimension>('customer');
  const { data: customers } = useLoad(salesApi.customerOptions, []);
  const { data: products } = useLoad(salesApi.productOptions, []);
  const load = useCallback(() => salesApi.report(filter), [filter]);
  const { data: rows, loading } = useLoad(load, []);

  const total = rows.reduce((sum, r) => sum + r.total, 0);
  const invoiceCount = new Set(rows.map((r) => r.invoice)).size;
  const totals = [...rows.reduce((map, r) => map.set(DIMENSIONS[dimension].value(r), (map.get(DIMENSIONS[dimension].value(r)) ?? 0) + r.total), new Map<string, number>())]
    .sort((a, b) => b[1] - a[1]);
  const top = totals[0]?.[1] ?? 0;

  return (
    <>
      <ControlPanel
        breadcrumbs={[{ label: __('Sales analysis') }]}
        exportData={() => ({
          fileName: `sales-report_${filter.from || 'start'}_${filter.to || 'today'}`,
          sheets: [
            listSheet(__('Sales lines'), COLUMNS, rows),
            { name: DIMENSIONS[dimension].label, columns: [{ label: DIMENSIONS[dimension].label }, { label: __('Revenue') }], rows: totals },
          ],
        })}
      />
      <div className="mx-auto max-w-7xl space-y-4 p-4">
        <div className="card grid gap-3 sm:grid-cols-4">
          <div>
            <label className="label" htmlFor="r-from">{__('From')}</label>
            <input id="r-from" type="date" className="input" value={filter.from} onChange={(e) => setFilter({ ...filter, from: e.target.value })} />
          </div>
          <div>
            <label className="label" htmlFor="r-to">{__('To')}</label>
            <input id="r-to" type="date" className="input" value={filter.to} onChange={(e) => setFilter({ ...filter, to: e.target.value })} />
          </div>
          <div>
            <label className="label" htmlFor="r-customer">{__('Customer')}</label>
            <Many2One id="r-customer" variant="boxed" placeholder={__('All customers')} options={customers.filter((c) => c.customer).map((c) => ({ value: c.id, label: c.name }))}
              value={filter.customerId === '' ? null : Number(filter.customerId)} onChange={(v) => setFilter({ ...filter, customerId: v === null ? '' : String(v) })} />
          </div>
          <div>
            <label className="label" htmlFor="r-product">{__('Product')}</label>
            <Many2One id="r-product" variant="boxed" placeholder={__('All products')} options={products.map((p) => ({ value: p.id, label: p.name }))}
              value={filter.productId === '' ? null : Number(filter.productId)} onChange={(v) => setFilter({ ...filter, productId: v === null ? '' : String(v) })} />
          </div>
        </div>

        <div className="grid gap-3 sm:grid-cols-3">
          <StatCard label={__('Revenue')} value={money(total)} />
          <StatCard label={__('Invoices')} value={invoiceCount} />
          <StatCard label={__('Average invoice')} value={money(invoiceCount === 0 ? 0 : total / invoiceCount)} />
        </div>

        <div className="card">
          <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
            <h2 className="font-semibold">{__('Revenue by :dimension').replace(':dimension', DIMENSIONS[dimension].label.toLowerCase())}</h2>
            <div className="flex overflow-hidden rounded border border-gray-300 text-sm">
              {(Object.keys(DIMENSIONS) as Dimension[]).map((d) => (
                <button key={d} type="button" aria-pressed={d === dimension} onClick={() => setDimension(d)} className={`px-3 py-1 ${d === dimension ? 'bg-brand text-white' : 'bg-white hover:bg-gray-50'}`}>
                  {DIMENSIONS[d].label}
                </button>
              ))}
            </div>
          </div>
          {totals.length === 0 && <p className="text-gray-500">{__('No sales match these filters.')}</p>}
          <ul className="space-y-1.5">
            {totals.slice(0, 10).map(([label, value]) => (
              <li key={label} className="grid grid-cols-[10rem_1fr_7rem] items-center gap-3 text-sm">
                <span className="truncate" title={label}>{label}</span>
                <span className="h-4 rounded-sm bg-gray-100">
                  <span className="block h-4 rounded-sm bg-accent" style={{ width: `${top === 0 ? 0 : (value / top) * 100}%` }} />
                </span>
                <span className="text-right tabular-nums">{money(value)}</span>
              </li>
            ))}
          </ul>
        </div>

        <div className="card p-0">
          <ListView rows={rows} columns={COLUMNS} rowKey={(r) => `${r.invoice}-${r.productId}-${r.quantity}`} loading={loading} />
        </div>
      </div>
    </>
  );
}
