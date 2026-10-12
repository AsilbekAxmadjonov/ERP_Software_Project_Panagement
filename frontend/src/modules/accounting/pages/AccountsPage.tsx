import { useState, type FormEvent } from 'react';
import { ApiError } from '../../../lib/http';
import { __ } from '../../../lib/i18n';
import { useLoad } from '../../../lib/useLoad';
import { accountingApi } from '../api';
import { AccountKind } from '../types';

const KIND_LABEL: Record<AccountKind, string> = {
  [AccountKind.ASSET]: __('Asset'),
  [AccountKind.LIABILITY]: __('Liability'),
  [AccountKind.EQUITY]: __('Equity'),
  [AccountKind.REVENUE]: __('Revenue'),
  [AccountKind.EXPENSE]: __('Expense'),
};

export function AccountsPage() {
  const { data: accounts, error: loadError, reload } = useLoad(accountingApi.accounts, []);
  const [form, setForm] = useState<{ code: string; name: string; type: AccountKind }>({ code: '', name: '', type: AccountKind.ASSET });
  const [error, setError] = useState<string | null>(null);

  const create = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    try {
      await accountingApi.createAccount(form);
      setForm({ code: '', name: '', type: AccountKind.ASSET });
      await reload();
    } catch (e) {
      setError(e instanceof ApiError ? e.message : __('Something went wrong.'));
    }
  };

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold">{__('Chart of accounts')}</h1>
      <form onSubmit={create} className="card grid gap-3 sm:grid-cols-4">
        <div>
          <label className="label" htmlFor="a-code">{__('Code')}</label>
          <input id="a-code" className="input" value={form.code} onChange={(e) => setForm({ ...form, code: e.target.value })} required />
        </div>
        <div>
          <label className="label" htmlFor="a-name">{__('Name')}</label>
          <input id="a-name" className="input" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
        </div>
        <div>
          <label className="label" htmlFor="a-type">{__('Type')}</label>
          <select id="a-type" className="input" value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value as AccountKind })}>
            {Object.values(AccountKind).map((kind) => (
              <option key={kind} value={kind}>{KIND_LABEL[kind]}</option>
            ))}
          </select>
        </div>
        <div className="flex items-end"><button className="btn w-full">{__('Add account')}</button></div>
        {(error ?? loadError) !== null && <p role="alert" className="text-sm text-red-600 sm:col-span-4">{error ?? loadError}</p>}
      </form>

      <div className="card overflow-x-auto p-0">
        <table className="min-w-full divide-y divide-slate-200">
          <thead><tr><th className="th">{__('Code')}</th><th className="th">{__('Name')}</th><th className="th">{__('Type')}</th><th className="th">{__('Status')}</th><th className="th" /></tr></thead>
          <tbody className="divide-y divide-slate-100">
            {accounts.map((a) => (
              <tr key={a.id}>
                <td className="td font-mono">{a.code}</td>
                <td className="td">{a.name}</td>
                <td className="td">{KIND_LABEL[a.type]}</td>
                <td className="td">{a.active ? __('Active') : __('Inactive')}</td>
                <td className="td text-right">
                  <button className="btn-secondary" onClick={() => void accountingApi.setAccountActive(a.code, !a.active).then(reload)}>
                    {a.active ? __('Deactivate') : __('Activate')}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
