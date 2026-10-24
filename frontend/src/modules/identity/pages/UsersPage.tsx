import { Plus } from 'lucide-react';
import { useState } from 'react';
import { __ } from '../../../lib/i18n';
import { useLoad } from '../../../lib/useLoad';
import { Badge } from '../../../ui/Badge';
import { ControlPanel } from '../../../ui/ControlPanel';
import { Dialog } from '../../../ui/Dialog';
import { Avatar } from '../../../ui/Kanban';
import { ListView, type Column } from '../../../ui/ListView';
import { useSearch } from '../../../ui/search';
import { useToast } from '../../../ui/Toast';
import { identityApi } from '../api';
import { ROLE_LABEL } from '../labels';
import { useSession } from '../session';
import { UserRole, type User } from '../types';

const EMPTY_FORM = { username: '', password: '', fullName: '', role: UserRole.MANAGER as UserRole };

export function UsersPage() {
  const toast = useToast();
  const { user: me } = useSession();
  const { data: users, loading, reload } = useLoad(identityApi.listUsers, []);
  const [creating, setCreating] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);

  const search = useSearch(users, {
    text: (u) => [u.username, u.fullName],
    filters: [
      ...Object.values(UserRole).map((r) => ({ key: r, group: 'role', label: ROLE_LABEL[r], test: (u: User) => u.role === r })),
      { key: 'active', group: 'state', label: __('Active'), test: (u) => u.active },
      { key: 'disabled', group: 'state', label: __('Disabled'), test: (u) => !u.active },
    ],
    groupBys: [{ key: 'role', label: __('Role'), value: (u) => ROLE_LABEL[u.role] }],
  });

  const update = (u: User, input: Parameters<typeof identityApi.updateUser>[1], message: string) =>
    void toast.run(() => identityApi.updateUser(u.id, input).then(reload), message);

  const columns: readonly Column<User>[] = [
    {
      key: 'name',
      label: __('Name'),
      render: (u) => <span className="inline-flex items-center gap-2 font-medium"><Avatar name={u.fullName ?? u.username} size="sm" />{u.fullName ?? u.username}</span>,
      sortValue: (u) => u.fullName ?? u.username,
    },
    { key: 'login', label: __('Login'), render: (u) => u.username, sortValue: (u) => u.username, className: 'text-gray-600' },
    {
      key: 'role',
      label: __('Role'),
      render: (u) => (
        <select
          aria-label={__('Role of :user').replace(':user', u.username)}
          className="o-input w-36"
          value={u.role}
          disabled={u.id === me?.id}
          onChange={(e) => update(u, { role: e.target.value as UserRole }, __('Role updated.'))}
        >
          {Object.values(UserRole).map((r) => (<option key={r} value={r}>{ROLE_LABEL[r]}</option>))}
        </select>
      ),
      sortValue: (u) => u.role,
    },
    { key: 'state', label: __('Status'), render: (u) => (u.active ? <Badge tone="green">{__('Active')}</Badge> : <Badge tone="gray">{__('Disabled')}</Badge>) },
    {
      key: 'actions',
      label: '',
      align: 'right',
      render: (u) =>
        u.id === me?.id ? (
          <span className="text-xs text-gray-400">{__('You')}</span>
        ) : (
          <button className="btn-link" onClick={() => update(u, { active: !u.active }, u.active ? __('User disabled.') : __('User enabled.'))}>
            {u.active ? __('Disable') : __('Enable')}
          </button>
        ),
    },
  ];

  const create = async () => {
    const ok = await toast.run(() => identityApi.createUser(form), __('User created.'));
    if (ok) {
      setCreating(false);
      setForm(EMPTY_FORM);
      await reload();
    }
  };

  return (
    <>
      <ControlPanel
        breadcrumbs={[{ label: __('Users') }]}
        actions={<button className="btn" onClick={() => setCreating(true)}><Plus className="h-4 w-4" />{__('New')}</button>}
        search={search}
      />
      <ListView rows={search.results} columns={columns} rowKey={(u) => u.id} loading={loading} groupBy={search.groupBy} rowClassName={(u) => (u.active ? '' : 'text-gray-400')} />
      {creating && (
        <Dialog title={__('New user')} onClose={() => setCreating(false)} onSubmit={create} submitLabel={__('Create user')}>
          <div className="grid gap-3 sm:grid-cols-2">
            <div className="sm:col-span-2">
              <label className="label" htmlFor="u-name">{__('Full name')}</label>
              <input id="u-name" className="input" value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} autoFocus />
            </div>
            <div>
              <label className="label" htmlFor="u-username">{__('Login')}</label>
              <input id="u-username" className="input" value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} autoComplete="off" required />
            </div>
            <div>
              <label className="label" htmlFor="u-password">{__('Password (min 8)')}</label>
              <input id="u-password" type="password" className="input" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} minLength={8} autoComplete="new-password" required />
            </div>
            <fieldset className="sm:col-span-2">
              <legend className="label">{__('Access rights')}</legend>
              <div className="flex flex-wrap gap-4">
                {Object.values(UserRole).map((r) => (
                  <label key={r} className="flex items-center gap-1.5">
                    <input type="radio" name="u-role" value={r} checked={form.role === r} onChange={() => setForm({ ...form, role: r })} className="accent-brand" />
                    {ROLE_LABEL[r]}
                  </label>
                ))}
              </div>
            </fieldset>
          </div>
        </Dialog>
      )}
    </>
  );
}
