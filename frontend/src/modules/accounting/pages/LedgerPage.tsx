import { useCallback, useState } from 'react';
import { money } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { useLoad } from '../../../lib/useLoad';
import { accountingApi } from '../api';

export function LedgerPage() {
  const { data: accounts } = useLoad(accountingApi.accounts, []);
  const [code, setCode] = useState('');
  const loadLedger = useCallback(() => (code === '' ? Promise.resolve([]) : accountingApi.accountLedger(code)), [code]);
  const { data: rows, error } = useLoad(loadLedger, []);

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold">{__('Company ledger')}</h1>
      <div className="max-w-md">
        <label className="label" htmlFor="l-account">{__('Account')}</label>
        <select id="l-account" className="input" value={code} onChange={(e) => setCode(e.target.value)}>
          <option value="">{__('Select account…')}</option>
          {accounts.map((a) => (
            <option key={a.code} value={a.code}>{a.code} · {a.name}</option>
          ))}
        </select>
      </div>
      {error !== null && <p role="alert" className="text-sm text-red-600">{error}</p>}
      <div className="card overflow-x-auto p-0">
        <table className="min-w-full divide-y divide-slate-200">
          <thead>
            <tr>
              <th className="th">{__('Date')}</th><th className="th">{__('Reference')}</th><th className="th">{__('Description')}</th>
              <th className="th text-right">{__('Debit')}</th><th className="th text-right">{__('Credit')}</th><th className="th text-right">{__('Balance')}</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {rows.map((r, i) => (
              <tr key={i}>
                <td className="td">{r.date}</td>
                <td className="td">{r.reference}</td>
                <td className="td">{r.description}</td>
                <td className="td text-right">{r.debit > 0 ? money(r.debit) : ''}</td>
                <td className="td text-right">{r.credit > 0 ? money(r.credit) : ''}</td>
                <td className="td text-right font-medium">{money(r.balance)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
