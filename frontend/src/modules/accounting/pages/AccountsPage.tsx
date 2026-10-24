import { Plus } from 'lucide-react';
import { useState } from 'react';
import { __ } from '../../../lib/i18n';
import { useLoad } from '../../../lib/useLoad';
import { Badge } from '../../../ui/Badge';
import { ControlPanel } from '../../../ui/ControlPanel';
import { Dialog } from '../../../ui/Dialog';
import { ListView, type Column } from '../../../ui/ListView';
import { useSearch } from '../../../ui/search';
import { useToast } from '../../../ui/Toast';
import { accountingApi } from '../api';
import { KIND_LABEL } from '../labels';
import { AccountKind, type Account } from '../types';

const EMPTY_FORM = { code: '', name: '', type: AccountKind.ASSET as AccountKind };

export function AccountsPage() {
  const toast = useToast();
  const { data: accounts, loading, reload } = useLoad(accountingApi.accounts, []);
  const [creating, setCreating] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);

  const search = useSearch(accounts, {
    text: (a) => [a.code, a.name],
    filters: [
      ...Object.values(AccountKind).map((k) => ({ key: k, group: 'type', label: KIND_LABEL[k], test: (a: Account) => a.type === k })),
      { key: 'active', group: 'state', label: __('Active'), test: (a) => a.active },
      { key: 'archived', group: 'state', label: __('Archived'), test: (a) => !a.active },
    ],
    groupBys: [{ key: 'type', label: __('Type'), value: (a) => KIND_LABEL[a.type] }],
  });

  const toggle = (a: Account) =>
    void toast.run(() => accountingApi.setAccountActive(a.code, !a.active).then(reload), a.active ? __('Account archived.') : __('Account restored.'));

  const columns: readonly Column<Account>[] = [
    { key: 'code', label: __('Code'), render: (a) => <span className="font-mono">{a.code}</span>, sortValue: (a) => a.code },
    { key: 'name', label: __('Account name'), render: (a) => a.name, sortValue: (a) => a.name },
    { key: 'type', label: __('Type'), render: (a) => KIND_LABEL[a.type], sortValue: (a) => a.type },
    { key: 'state', label: __('Status'), render: (a) => (a.active ? <Badge tone="green">{__('Active')}</Badge> : <Badge tone="gray">{__('Archived')}</Badge>) },
    {
      key: 'actions',
      label: '',
      align: 'right',
      render: (a) => <button className="btn-link" onClick={() => toggle(a)}>{a.active ? __('Archive') : __('Restore')}</button>,
    },
  ];

  const create = async () => {
    const ok = await toast.run(() => accountingApi.createAccount(form), __('Account created.'));
    if (ok) {
      setCreating(false);
      setForm(EMPTY_FORM);
      await reload();
    }
  };

  return (
    <>
      <ControlPanel
        breadcrumbs={[{ label: __('Chart of accounts') }]}
        actions={<button className="btn" onClick={() => setCreating(true)}><Plus className="h-4 w-4" />{__('New')}</button>}
        search={search}
      />
      <ListView rows={search.results} columns={columns} rowKey={(a) => a.id} loading={loading} groupBy={search.groupBy} rowClassName={(a) => (a.active ? '' : 'text-gray-400')} />
      {creating && (
        <Dialog title={__('New account')} onClose={() => setCreating(false)} onSubmit={create} submitLabel={__('Save')}>
          <div className="grid gap-3 sm:grid-cols-[8rem_1fr]">
            <div>
              <label className="label" htmlFor="acc-code">{__('Code')}</label>
              <input id="acc-code" className="input font-mono" value={form.code} onChange={(e) => setForm({ ...form, code: e.target.value })} required autoFocus />
            </div>
            <div>
              <label className="label" htmlFor="acc-name">{__('Account name')}</label>
              <input id="acc-name" className="input" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
            </div>
            <div className="sm:col-span-2">
              <label className="label" htmlFor="acc-type">{__('Type')}</label>
              <select id="acc-type" className="input" value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value as AccountKind })}>
                {Object.values(AccountKind).map((k) => (<option key={k} value={k}>{KIND_LABEL[k]}</option>))}
              </select>
            </div>
          </div>
        </Dialog>
      )}
    </>
  );
}
