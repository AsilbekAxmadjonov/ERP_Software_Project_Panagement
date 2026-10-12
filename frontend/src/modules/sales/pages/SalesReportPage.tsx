import { useCallback, useState } from 'react';
import { money, quantity } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { useLoad } from '../../../lib/useLoad';
import { salesApi, type ReportFilter } from '../api';

const NO_FILTER: ReportFilter = { from: '', to: '', customerId: '', productId: '' };

export function SalesReportPage() {
  const [filter, setFilter] = useState<ReportFilter>(NO_FILTER);
  const { data: customers } = useLoad(salesApi.customerOptions, []);
  const { data: products } = useLoad(salesApi.productOptions, []);
  const load = useCallback(() => salesApi.report(filter), [filter]);
  const { data: rows, error } = useLoad(load, []);
  const total = rows.reduce((sum, r) => sum + r.total, 0);

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold">{__('Sales report')}</h1>
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
          <select id="r-customer" className="input" value={filter.customerId} onChange={(e) => setFilter({ ...filter, customerId: e.target.value })}>
            <option value="">{__('All customers')}</option>
            {customers.filter((c) => c.customer).map((c) => (<option key={c.id} value={c.id}>{c.name}</option>))}
          </select>
        </div>
        <div>
          <label className="label" htmlFor="r-product">{__('Product')}</label>
          <select id="r-product" className="input" value={filter.productId} onChange={(e) => setFilter({ ...filter, productId: e.target.value })}>
            <option value="">{__('All products')}</option>
            {products.map((p) => (<option key={p.id} value={p.id}>{p.name}</option>))}
          </select>
        </div>
      </div>
      {error !== null && <p role="alert" className="text-sm text-red-600">{error}</p>}
      <div className="card overflow-x-auto p-0">
        <table className="min-w-full divide-y divide-slate-200">
          <thead>
            <tr>
              <th className="th">{__('Date')}</th><th className="th">{__('Invoice')}</th><th className="th">{__('Customer')}</th>
              <th className="th">{__('Product')}</th><th className="th text-right">{__('Quantity')}</th>
              <th className="th text-right">{__('Price')}</th><th className="th text-right">{__('Total')}</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {rows.map((r, i) => (
              <tr key={i}>
                <td className="td">{r.date}</td><td className="td font-mono">{r.invoice}</td><td className="td">{r.customer}</td><td className="td">{r.product}</td>
                <td className="td text-right">{quantity(r.quantity)} {r.unit.toLowerCase()}</td>
                <td className="td text-right">{money(r.unitPrice)}</td><td className="td text-right">{money(r.total)}</td>
              </tr>
            ))}
          </tbody>
          <tfoot><tr className="font-semibold"><td className="td" colSpan={6}>{__('Total')}</td><td className="td text-right">{money(total)}</td></tr></tfoot>
        </table>
      </div>
    </div>
  );
}
