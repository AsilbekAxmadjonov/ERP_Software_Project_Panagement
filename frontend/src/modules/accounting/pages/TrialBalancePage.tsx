import { money } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { useLoad } from '../../../lib/useLoad';
import { accountingApi } from '../api';
import type { TrialBalance } from '../types';

const EMPTY: TrialBalance = { rows: [], totalDebit: 0, totalCredit: 0 };

export function TrialBalancePage() {
  const { data, error } = useLoad(accountingApi.trialBalance, EMPTY);
  const balanced = Math.abs(data.totalDebit - data.totalCredit) < 0.005;

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold">{__('Trial balance')}</h1>
        <span className={balanced ? 'rounded bg-green-100 px-2 py-1 text-sm text-green-800' : 'rounded bg-red-100 px-2 py-1 text-sm text-red-800'}>
          {balanced ? __('Ledger is balanced') : __('Ledger is NOT balanced')}
        </span>
      </div>
      {error !== null && <p role="alert" className="text-sm text-red-600">{error}</p>}
      <div className="card overflow-x-auto p-0">
        <table className="min-w-full divide-y divide-slate-200">
          <thead><tr><th className="th">{__('Code')}</th><th className="th">{__('Account')}</th><th className="th text-right">{__('Debit')}</th><th className="th text-right">{__('Credit')}</th></tr></thead>
          <tbody className="divide-y divide-slate-100">
            {data.rows.map((r) => (
              <tr key={r.code}>
                <td className="td font-mono">{r.code}</td>
                <td className="td">{r.name}</td>
                <td className="td text-right">{r.debit > 0 ? money(r.debit) : ''}</td>
                <td className="td text-right">{r.credit > 0 ? money(r.credit) : ''}</td>
              </tr>
            ))}
          </tbody>
          <tfoot>
            <tr className="font-semibold">
              <td className="td" colSpan={2}>{__('Total')}</td>
              <td className="td text-right">{money(data.totalDebit)}</td>
              <td className="td text-right">{money(data.totalCredit)}</td>
            </tr>
          </tfoot>
        </table>
      </div>
    </div>
  );
}
