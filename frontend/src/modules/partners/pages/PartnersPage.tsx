import { useCallback, useEffect, useState, type FormEvent } from 'react';
import { ApiError } from '../../../lib/http';
import { __ } from '../../../lib/i18n';
import { partnersApi } from '../api';
import { PartnerKind, type Partner, type PartnerInput } from '../types';

const KIND_LABEL: Record<PartnerKind, string> = {
  [PartnerKind.CUSTOMER]: __('Customer'),
  [PartnerKind.SUPPLIER]: __('Supplier'),
  [PartnerKind.BOTH]: __('Customer & supplier'),
};

const EMPTY: PartnerInput = { name: '', type: PartnerKind.CUSTOMER, contactPerson: '', phone: '', email: '', address: '', taxId: '', active: true };

const toInput = (p: Partner): PartnerInput => ({
  name: p.name,
  type: p.type,
  contactPerson: p.contactPerson ?? '',
  phone: p.phone ?? '',
  email: p.email ?? '',
  address: p.address ?? '',
  taxId: p.taxId ?? '',
  active: p.active,
});

export function PartnersPage() {
  const [partners, setPartners] = useState<Partner[]>([]);
  const [form, setForm] = useState<PartnerInput>(EMPTY);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(() => partnersApi.list().then(setPartners).catch((e: ApiError) => setError(e.message)), []);
  useEffect(() => {
    void load();
  }, [load]);

  const reset = () => {
    setForm(EMPTY);
    setEditingId(null);
  };

  const save = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    try {
      if (editingId === null) await partnersApi.create(form);
      else await partnersApi.update(editingId, form);
      reset();
      await load();
    } catch (e) {
      setError(e instanceof ApiError ? e.message : __('Something went wrong.'));
    }
  };

  const field = (key: keyof PartnerInput, label: string, required = false) => (
    <div>
      <label className="label" htmlFor={`p-${key}`}>{label}</label>
      <input id={`p-${key}`} className="input" value={String(form[key])} onChange={(e) => setForm({ ...form, [key]: e.target.value })} required={required} />
    </div>
  );

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold">{__('Partners')}</h1>

      <form onSubmit={save} className="card grid gap-3 sm:grid-cols-4">
        {field('name', __('Name'), true)}
        <div>
          <label className="label" htmlFor="p-type">{__('Type')}</label>
          <select id="p-type" className="input" value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value as PartnerKind })}>
            {Object.values(PartnerKind).map((kind) => (
              <option key={kind} value={kind}>{KIND_LABEL[kind]}</option>
            ))}
          </select>
        </div>
        {field('contactPerson', __('Contact person'))}
        {field('phone', __('Phone'))}
        {field('email', __('Email'))}
        {field('address', __('Address'))}
        {field('taxId', __('Tax ID'))}
        <div className="flex items-end gap-2">
          <button className="btn flex-1">{editingId === null ? __('Add partner') : __('Save changes')}</button>
          {editingId !== null && <button type="button" className="btn-secondary" onClick={reset}>{__('Cancel')}</button>}
        </div>
        {error !== null && <p role="alert" className="text-sm text-red-600 sm:col-span-4">{error}</p>}
      </form>

      <div className="card overflow-x-auto p-0">
        <table className="min-w-full divide-y divide-slate-200">
          <thead>
            <tr>
              <th className="th">{__('Name')}</th>
              <th className="th">{__('Type')}</th>
              <th className="th">{__('Contact')}</th>
              <th className="th">{__('Phone')}</th>
              <th className="th">{__('Status')}</th>
              <th className="th" />
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {partners.map((p) => (
              <tr key={p.id}>
                <td className="td font-medium">{p.name}</td>
                <td className="td">{KIND_LABEL[p.type]}</td>
                <td className="td">{p.contactPerson}</td>
                <td className="td">{p.phone}</td>
                <td className="td">{p.active ? __('Active') : __('Inactive')}</td>
                <td className="td text-right">
                  <button className="btn-secondary" onClick={() => { setEditingId(p.id); setForm(toInput(p)); }}>{__('Edit')}</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
