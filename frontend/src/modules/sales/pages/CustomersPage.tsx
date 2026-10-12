import { useCallback, useState } from 'react';
import { money } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { useLoad } from '../../../lib/useLoad';
import { salesApi } from '../api';
import type { LedgerRow } from '../types';

export function CustomersPage() {
  const { data: customers, error } = useLoad(salesApi.customerBalances, []);
  const [selected, setSelected] = useState<number | null>(null);
  const loadLedger = useCallback(() => (selected === null ? Promise.resolve<LedgerRow[]>([]) : salesApi.customerLedger(selected)), [selected]);
  const { data: ledger } = useLoad(loadLedger, []);

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold">{__('Customers and balances')}</h1>
      {error !== null && <p role="alert" className="text-sm text-red-600">{error}</p>}

      <div className="card overflow-x-auto p-0">
        <table className="min-w-full divide-y divide-slate-200">
          <thead>
            <tr><th className="th">{__('Customer')}</th><th className="th">{__('Phone')}</th><th className="th text-right">{__('Owes us')}</th><th className="th" /></tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {customers.map((c) => (
              <tr key={c.partnerId} className={selected === c.partnerId ? 'bg-indigo-50' : ''}>
                <td className="td font-medium">{c.name}</td><td className="td">{c.phone}</td>
                <td className={`td text-right ${c.balance > 0 ? 'text-red-700' : ''}`}>{money(c.balance)}</td>
                <td className="td text-right"><button className="btn-secondary" onClick={() => setSelected(c.partnerId)}>{__('Ledger')}</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {selected !== null && (
        <div className="space-y-2">
          <h2 className="font-medium">{__('Customer ledger')}</h2>
          <div className="card overflow-x-auto p-0">
            <table className="min-w-full divide-y divide-slate-200">
              <thead>
                <tr>
                  <th className="th">{__('Date')}</th><th className="th">{__('Reference')}</th><th className="th">{__('Description')}</th>
                  <th className="th text-right">{__('Debit')}</th><th className="th text-right">{__('Credit')}</th><th className="th text-right">{__('Balance')}</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {ledger.map((r, i) => (
                  <tr key={i}>
                    <td className="td">{r.date}</td><td className="td">{r.reference}</td><td className="td">{r.description}</td>
                    <td className="td text-right">{r.debit > 0 ? money(r.debit) : ''}</td>
                    <td className="td text-right">{r.credit > 0 ? money(r.credit) : ''}</td>
                    <td className="td text-right font-medium">{money(r.balance)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
