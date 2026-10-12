import { Fragment, useState, type FormEvent } from 'react';
import { ApiError } from '../../../lib/http';
import { money, quantity, today } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { useLoad } from '../../../lib/useLoad';
import { salesApi } from '../api';
import { PaymentProgress } from '../types';

const STATUS_LABEL: Record<PaymentProgress, string> = {
  [PaymentProgress.UNPAID]: __('Unpaid'),
  [PaymentProgress.PARTIAL]: __('Partially paid'),
  [PaymentProgress.PAID]: __('Paid'),
};

const STATUS_STYLE: Record<PaymentProgress, string> = {
  [PaymentProgress.UNPAID]: 'bg-red-100 text-red-800',
  [PaymentProgress.PARTIAL]: 'bg-amber-100 text-amber-800',
  [PaymentProgress.PAID]: 'bg-green-100 text-green-800',
};

interface ItemDraft {
  productId: string;
  quantity: string;
  unitPrice: string;
}

const emptyItem = (): ItemDraft => ({ productId: '', quantity: '', unitPrice: '' });

export function InvoicesPage() {
  const { data: invoices, error: loadError, reload } = useLoad(salesApi.invoices, []);
  const { data: customers } = useLoad(salesApi.customerOptions, []);
  const { data: products, reload: reloadProducts } = useLoad(salesApi.productOptions, []);
  const [customerId, setCustomerId] = useState('');
  const [date, setDate] = useState(today());
  const [items, setItems] = useState<ItemDraft[]>([emptyItem()]);
  const [error, setError] = useState<string | null>(null);
  const [open, setOpen] = useState<number | null>(null);

  const productById = (id: string) => products.find((p) => String(p.id) === id);
  const draftTotal = items.reduce((sum, item) => {
    const product = productById(item.productId);
    const price = item.unitPrice === '' ? (product?.salePrice ?? 0) : Number(item.unitPrice);
    return sum + Number(item.quantity || 0) * price;
  }, 0);

  const setItem = (index: number, patch: Partial<ItemDraft>) => setItems(items.map((it, i) => (i === index ? { ...it, ...patch } : it)));

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    try {
      await salesApi.issueInvoice({
        customerId: Number(customerId),
        date,
        items: items.map((it) => ({
          productId: Number(it.productId),
          quantity: Number(it.quantity),
          unitPrice: it.unitPrice === '' ? undefined : Number(it.unitPrice),
        })),
      });
      setItems([emptyItem()]);
      await Promise.all([reload(), reloadProducts()]);
    } catch (e) {
      setError(e instanceof ApiError ? e.message : __('Something went wrong.'));
    }
  };

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold">{__('Sales invoices')}</h1>

      <form onSubmit={submit} className="card space-y-3">
        <div className="grid gap-3 sm:grid-cols-3">
          <div className="sm:col-span-2">
            <label className="label" htmlFor="i-customer">{__('Customer')}</label>
            <select id="i-customer" className="input" value={customerId} onChange={(e) => setCustomerId(e.target.value)} required>
              <option value="">{__('Select customer…')}</option>
              {customers.filter((c) => c.customer && c.active).map((c) => (<option key={c.id} value={c.id}>{c.name}</option>))}
            </select>
          </div>
          <div>
            <label className="label" htmlFor="i-date">{__('Date')}</label>
            <input id="i-date" type="date" className="input" value={date} onChange={(e) => setDate(e.target.value)} required />
          </div>
        </div>

        {items.map((item, index) => {
          const product = productById(item.productId);
          return (
            <div key={index} className="grid gap-2 sm:grid-cols-[3fr_1fr_1fr_auto]">
              <select aria-label={__('Product')} className="input" value={item.productId} onChange={(e) => setItem(index, { productId: e.target.value })} required>
                <option value="">{__('Select product…')}</option>
                {products.filter((p) => p.active).map((p) => (
                  <option key={p.id} value={p.id}>{p.name} ({quantity(p.quantity)} {p.unit.toLowerCase()} {__('in stock')})</option>
                ))}
              </select>
              <input aria-label={__('Quantity')} placeholder={__('Quantity')} type="number" min="0.001" step="0.001" className="input" value={item.quantity} onChange={(e) => setItem(index, { quantity: e.target.value })} required />
              <input aria-label={__('Price')} placeholder={product ? money(product.salePrice) : __('Price')} type="number" min="0" step="0.01" className="input" value={item.unitPrice} onChange={(e) => setItem(index, { unitPrice: e.target.value })} />
              <button type="button" className="btn-secondary" disabled={items.length <= 1} onClick={() => setItems(items.filter((_, i) => i !== index))}>×</button>
            </div>
          );
        })}

        <div className="flex flex-wrap items-center justify-between gap-3">
          <button type="button" className="btn-secondary" onClick={() => setItems([...items, emptyItem()])}>{__('Add item')}</button>
          <div className="flex items-center gap-4">
            <span className="text-sm">{__('Total')}: <b>{money(draftTotal)}</b></span>
            <button className="btn">{__('Issue invoice')}</button>
          </div>
        </div>
        {(error ?? loadError) !== null && <p role="alert" className="text-sm text-red-600">{error ?? loadError}</p>}
      </form>

      <div className="card overflow-x-auto p-0">
        <table className="min-w-full divide-y divide-slate-200">
          <thead>
            <tr>
              <th className="th">{__('Number')}</th><th className="th">{__('Date')}</th><th className="th">{__('Customer')}</th>
              <th className="th text-right">{__('Total')}</th><th className="th text-right">{__('Paid')}</th>
              <th className="th text-right">{__('Outstanding')}</th><th className="th">{__('Status')}</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {invoices.map((inv) => (
              <Fragment key={inv.id}>
                <tr>
                  <td className="td"><button className="font-mono underline" onClick={() => setOpen(open === inv.id ? null : inv.id)}>{inv.number}</button></td>
                  <td className="td">{inv.invoiceDate}</td><td className="td">{inv.customerName}</td>
                  <td className="td text-right">{money(inv.total)}</td><td className="td text-right">{money(inv.paid)}</td>
                  <td className="td text-right">{money(inv.outstanding)}</td>
                  <td className="td"><span className={`rounded px-2 py-0.5 text-xs ${STATUS_STYLE[inv.status]}`}>{STATUS_LABEL[inv.status]}</span></td>
                </tr>
                {open === inv.id && (
                  <tr>
                    <td colSpan={7} className="bg-slate-50 px-6 py-2">
                      {inv.lines.map((l, i) => (
                        <div key={i} className="grid grid-cols-[3fr_1fr_1fr_1fr] gap-2 text-sm">
                          <span>{l.productName}</span>
                          <span className="text-right">{quantity(l.quantity)} {l.unit.toLowerCase()}</span>
                          <span className="text-right">{money(l.unitPrice)}</span>
                          <span className="text-right">{money(l.lineTotal)}</span>
                        </div>
                      ))}
                    </td>
                  </tr>
                )}
              </Fragment>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
