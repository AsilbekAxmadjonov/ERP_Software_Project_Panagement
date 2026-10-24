import { useCallback, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { date, money, quantity } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { route } from '../../../lib/routes';
import { useLoad } from '../../../lib/useLoad';
import { ControlPanel } from '../../../ui/ControlPanel';
import { StatCard } from '../../../ui/EmptyState';
import { ListView, type Column } from '../../../ui/ListView';
import { Many2One } from '../../../ui/Many2One';
import { purchaseApi, type ReportFilter } from '../api';
import type { PurchaseReportRow } from '../types';

const NO_FILTER: ReportFilter = { from: '', to: '', supplierId: '', productId: '' };

type Dimension = 'supplier' | 'product' | 'month';

const DIMENSIONS: Record<Dimension, { label: string; value: (r: PurchaseReportRow) => string }> = {
  supplier: { label: __('Vendor'), value: (r) => r.supplier },
  product: { label: __('Product'), value: (r) => r.product },
  month: { label: __('Month'), value: (r) => r.date.slice(0, 7) },
};

const COLUMNS: readonly Column<PurchaseReportRow>[] = [
  { key: 'date', label: __('Received'), render: (r) => date(r.date), sortValue: (r) => r.date },
  { key: 'order', label: __('Order'), render: (r) => r.order, sortValue: (r) => r.order },
  { key: 'supplier', label: __('Vendor'), render: (r) => r.supplier, sortValue: (r) => r.supplier },
  { key: 'product', label: __('Product'), render: (r) => r.product, sortValue: (r) => r.product },
  { key: 'qty', label: __('Quantity'), align: 'right', render: (r) => `${quantity(r.quantity)} ${r.unit.toLowerCase()}`, sortValue: (r) => r.quantity },
  { key: 'cost', label: __('Unit cost'), align: 'right', render: (r) => money(r.unitCost) },
  { key: 'total', label: __('Total'), align: 'right', render: (r) => money(r.total), sortValue: (r) => r.total, sum: (r) => r.total, formatSum: money },
];

export function PurchaseReportPage() {
  const navigate = useNavigate();
  const [filter, setFilter] = useState<ReportFilter>(NO_FILTER);
  const [dimension, setDimension] = useState<Dimension>('supplier');
  const { data: suppliers } = useLoad(purchaseApi.supplierOptions, []);
  const { data: products } = useLoad(purchaseApi.productOptions, []);
  const load = useCallback(() => purchaseApi.report(filter), [filter]);
  const { data: rows, loading } = useLoad(load, []);

  const total = rows.reduce((sum, r) => sum + r.total, 0);
  const orderCount = new Set(rows.map((r) => r.orderId)).size;
  const vendorCount = new Set(rows.map((r) => r.supplierId)).size;
  const totals = [...rows.reduce((map, r) => map.set(DIMENSIONS[dimension].value(r), (map.get(DIMENSIONS[dimension].value(r)) ?? 0) + r.total), new Map<string, number>())]
    .sort((a, b) => b[1] - a[1]);
  const top = totals[0]?.[1] ?? 0;

  return (
    <>
      <ControlPanel breadcrumbs={[{ label: __('Purchase analysis') }]} />
      <div className="mx-auto max-w-7xl space-y-4 p-4">
        <div className="card no-print grid gap-3 sm:grid-cols-4">
          <div>
            <label className="label" htmlFor="p-from">{__('Received from')}</label>
            <input id="p-from" type="date" className="input" value={filter.from} onChange={(e) => setFilter({ ...filter, from: e.target.value })} />
          </div>
          <div>
            <label className="label" htmlFor="p-to">{__('Received to')}</label>
            <input id="p-to" type="date" className="input" value={filter.to} onChange={(e) => setFilter({ ...filter, to: e.target.value })} />
          </div>
          <div>
            <label className="label" htmlFor="p-supplier">{__('Vendor')}</label>
            <Many2One id="p-supplier" variant="boxed" placeholder={__('All vendors')} options={suppliers.filter((s) => s.supplier).map((s) => ({ value: s.id, label: s.name }))}
              value={filter.supplierId === '' ? null : Number(filter.supplierId)} onChange={(v) => setFilter({ ...filter, supplierId: v === null ? '' : String(v) })} />
          </div>
          <div>
            <label className="label" htmlFor="p-product">{__('Product')}</label>
            <Many2One id="p-product" variant="boxed" placeholder={__('All products')} options={products.map((p) => ({ value: p.id, label: p.name }))}
              value={filter.productId === '' ? null : Number(filter.productId)} onChange={(v) => setFilter({ ...filter, productId: v === null ? '' : String(v) })} />
          </div>
        </div>

        <div className="grid gap-3 sm:grid-cols-4">
          <StatCard label={__('Purchased')} value={money(total)} />
          <StatCard label={__('Orders received')} value={orderCount} />
          <StatCard label={__('Average order')} value={money(orderCount === 0 ? 0 : total / orderCount)} />
          <StatCard label={__('Vendors')} value={vendorCount} />
        </div>

        <div className="card">
          <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
            <h2 className="font-semibold">{__('Purchases by :dimension').replace(':dimension', DIMENSIONS[dimension].label.toLowerCase())}</h2>
            <div className="no-print flex overflow-hidden rounded border border-gray-300 text-sm">
              {(Object.keys(DIMENSIONS) as Dimension[]).map((d) => (
                <button key={d} type="button" aria-pressed={d === dimension} onClick={() => setDimension(d)} className={`px-3 py-1 ${d === dimension ? 'bg-brand text-white' : 'bg-white hover:bg-gray-50'}`}>
                  {DIMENSIONS[d].label}
                </button>
              ))}
            </div>
          </div>
          {totals.length === 0 && <p className="text-gray-500">{__('No received purchases match these filters.')}</p>}
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
          <ListView rows={rows} columns={COLUMNS} rowKey={(r) => `${r.orderId}-${r.productId}-${r.quantity}`} loading={loading}
            onOpen={(r) => navigate(route('purchase.orders.show', { id: r.orderId }))} />
        </div>
      </div>
    </>
  );
}
