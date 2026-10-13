import { useState, type FormEvent } from 'react';
import { ApiError } from '../../../lib/http';
import { money, today } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { useLoad } from '../../../lib/useLoad';
import { salesApi } from '../api';
import { PaymentChannel } from '../types';

const CHANNEL_LABEL: Record<PaymentChannel, string> = {
  [PaymentChannel.CASH]: __('Cash (Kassa)'),
  [PaymentChannel.BANK]: __('Bank'),
};

export function PaymentsPage() {
  const { data: payments, error: loadError, reload } = useLoad(salesApi.payments, []);
  const { data: customers } = useLoad(salesApi.customerOptions, []);
  const { data: invoices, reload: reloadInvoices } = useLoad(salesApi.invoices, []);
  const [form, setForm] = useState({ customerId: '', invoiceId: '', amount: '', method: PaymentChannel.CASH as PaymentChannel, date: today(), note: '' });
  const [error, setError] = useState<string | null>(null);

  const openInvoices = invoices.filter((i) => String(i.customerId) === form.customerId && i.outstanding > 0);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    try {
      await salesApi.recordPayment({
        customerId: Number(form.customerId),
        invoiceId: form.invoiceId === '' ? undefined : Number(form.invoiceId),
        amount: Number(form.amount),
        method: form.method,
        date: form.date,
        note: form.note,
      });
      setForm({ ...form, invoiceId: '', amount: '', note: '' });
      await Promise.all([reload(), reloadInvoices()]);
    } catch (e) {
      setError(e instanceof ApiError ? e.message : __('Something went wrong.'));
    }
  };

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold">{__('Customer payments')}</h1>

      <form onSubmit={submit} className="card grid gap-3 sm:grid-cols-3">
        <div>
          <label className="label" htmlFor="pay-customer">{__('Customer')}</label>
          <select id="pay-customer" className="input" value={form.customerId} onChange={(e) => setForm({ ...form, customerId: e.target.value, invoiceId: '' })} required>
            <option value="">{__('Select customer…')}</option>
            {customers.filter((c) => c.customer).map((c) => (<option key={c.id} value={c.id}>{c.name}</option>))}
          </select>
        </div>
        <div>
          <label className="label" htmlFor="pay-invoice">{__('Invoice (optional)')}</label>
          <select id="pay-invoice" className="input" value={form.invoiceId} onChange={(e) => setForm({ ...form, invoiceId: e.target.value })}>
            <option value="">{__('Advance / on account')}</option>
            {openInvoices.map((i) => (<option key={i.id} value={i.id}>{i.number} · {money(i.outstanding)}</option>))}
          </select>
        </div>
        <div>
          <label className="label" htmlFor="pay-amount">{__('Amount')}</label>
          <input id="pay-amount" type="number" min="0.01" step="0.01" className="input" value={form.amount} onChange={(e) => setForm({ ...form, amount: e.target.value })} required />
        </div>
        <div>
          <label className="label" htmlFor="pay-method">{__('Paid into')}</label>
          <select id="pay-method" className="input" value={form.method} onChange={(e) => setForm({ ...form, method: e.target.value as PaymentChannel })}>
            {Object.values(PaymentChannel).map((m) => (<option key={m} value={m}>{CHANNEL_LABEL[m]}</option>))}
          </select>
        </div>
        <div>
          <label className="label" htmlFor="pay-date">{__('Date')}</label>
          <input id="pay-date" type="date" className="input" value={form.date} onChange={(e) => setForm({ ...form, date: e.target.value })} required />
        </div>
        <div>
          <label className="label" htmlFor="pay-note">{__('Note')}</label>
          <input id="pay-note" className="input" value={form.note} onChange={(e) => setForm({ ...form, note: e.target.value })} />
        </div>
        <button className="btn sm:col-span-3">{__('Record payment')}</button>
        {(error ?? loadError) !== null && <p role="alert" className="text-sm text-red-600 sm:col-span-3">{error ?? loadError}</p>}
      </form>

      <div className="card overflow-x-auto p-0">
        <table className="min-w-full divide-y divide-slate-200">
          <thead>
            <tr>
              <th className="th">{__('Date')}</th><th className="th">{__('Customer')}</th><th className="th">{__('Invoice')}</th>
              <th className="th">{__('Paid into')}</th><th className="th text-right">{__('Amount')}</th><th className="th">{__('Note')}</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {payments.map((p) => (
              <tr key={p.id}>
                <td className="td">{p.date}</td><td className="td">{p.customerName}</td><td className="td font-mono">{p.invoiceNumber}</td>
                <td className="td">{CHANNEL_LABEL[p.method]}</td><td className="td text-right">{money(p.amount)}</td><td className="td">{p.note}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
