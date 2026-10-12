import { useCallback, useEffect, useState, type FormEvent } from 'react';
import { ApiError } from '../../../lib/http';
import { __ } from '../../../lib/i18n';
import { identityApi } from '../api';
import { UserRole, type User } from '../types';

const ROLE_LABEL: Record<UserRole, string> = {
  [UserRole.CEO]: __('CEO'),
  [UserRole.MANAGER]: __('Manager'),
  [UserRole.ACCOUNTANT]: __('Accountant'),
};

const EMPTY_FORM = { username: '', password: '', fullName: '', role: UserRole.MANAGER as UserRole };

export function UsersPage() {
  const [users, setUsers] = useState<User[]>([]);
  const [form, setForm] = useState(EMPTY_FORM);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(() => identityApi.listUsers().then(setUsers).catch((e: ApiError) => setError(e.message)), []);
  useEffect(() => {
    void load();
  }, [load]);

  const create = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    try {
      await identityApi.createUser(form);
      setForm(EMPTY_FORM);
      await load();
    } catch (e) {
      setError(e instanceof ApiError ? e.message : __('Something went wrong.'));
    }
  };

  const toggleActive = async (user: User) => {
    await identityApi.updateUser(user.id, { active: !user.active });
    await load();
  };

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold">{__('Users')}</h1>

      <form onSubmit={create} className="card grid gap-3 sm:grid-cols-5">
        <div>
          <label className="label" htmlFor="u-username">{__('Username')}</label>
          <input id="u-username" className="input" value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} required />
        </div>
        <div>
          <label className="label" htmlFor="u-name">{__('Full name')}</label>
          <input id="u-name" className="input" value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} />
        </div>
        <div>
          <label className="label" htmlFor="u-password">{__('Password (min 8)')}</label>
          <input id="u-password" type="password" className="input" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} minLength={8} required />
        </div>
        <div>
          <label className="label" htmlFor="u-role">{__('Role')}</label>
          <select id="u-role" className="input" value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value as UserRole })}>
            {Object.values(UserRole).map((role) => (
              <option key={role} value={role}>{ROLE_LABEL[role]}</option>
            ))}
          </select>
        </div>
        <div className="flex items-end">
          <button className="btn w-full">{__('Add user')}</button>
        </div>
        {error !== null && <p role="alert" className="text-sm text-red-600 sm:col-span-5">{error}</p>}
      </form>

      <div className="card overflow-x-auto p-0">
        <table className="min-w-full divide-y divide-slate-200">
          <thead>
            <tr>
              <th className="th">{__('Username')}</th>
              <th className="th">{__('Full name')}</th>
              <th className="th">{__('Role')}</th>
              <th className="th">{__('Status')}</th>
              <th className="th" />
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {users.map((user) => (
              <tr key={user.id}>
                <td className="td font-medium">{user.username}</td>
                <td className="td">{user.fullName}</td>
                <td className="td">{ROLE_LABEL[user.role]}</td>
                <td className="td">{user.active ? __('Active') : __('Disabled')}</td>
                <td className="td text-right">
                  <button className="btn-secondary" onClick={() => void toggleActive(user)}>
                    {user.active ? __('Disable') : __('Enable')}
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
