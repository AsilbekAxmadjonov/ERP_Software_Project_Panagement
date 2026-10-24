import { Save, Undo2 } from 'lucide-react';
import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { __ } from '../../../lib/i18n';
import { route } from '../../../lib/routes';
import { useLoad } from '../../../lib/useLoad';
import { Ribbon } from '../../../ui/Badge';
import { ControlPanel } from '../../../ui/ControlPanel';
import { Columns, Field, FieldGroup, Sheet } from '../../../ui/Form';
import { Avatar } from '../../../ui/Kanban';
import { useToast } from '../../../ui/Toast';
import { partnersApi } from '../api';
import { KIND } from '../labels';
import { PartnerKind, type Partner, type PartnerInput } from '../types';

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

type TextField = 'contactPerson' | 'phone' | 'email' | 'address' | 'taxId';

export function ContactFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const toast = useToast();
  const partnerId = id === undefined ? null : Number(id);
  const loadPartner = useCallback(() => (partnerId === null ? Promise.resolve(null) : partnersApi.get(partnerId)), [partnerId]);
  const { data: partner, error, reload } = useLoad<Partner | null>(loadPartner, null);
  const [form, setForm] = useState<PartnerInput>(EMPTY);
  const [processing, setProcessing] = useState(false);

  useEffect(() => {
    if (partner) setForm(toInput(partner));
  }, [partner]);

  if (error !== null) return <p className="p-6 text-red-600" role="alert">{error}</p>;
  if (partnerId !== null && partner === null) return <p className="p-6 text-gray-500">{__('Loading…')}</p>;

  const dirty = partner === null || JSON.stringify(toInput(partner)) !== JSON.stringify(form);

  const save = async (next: PartnerInput = form) => {
    setProcessing(true);
    let saved: Partner | undefined;
    const ok = await toast.run(async () => {
      saved = partner === null ? await partnersApi.create(next) : await partnersApi.update(partner.id, next);
    }, __('Contact saved.'));
    setProcessing(false);
    if (!ok || !saved) return;
    if (partner === null) navigate(route('partners.show', { id: saved.id }), { replace: true });
    else await reload();
  };

  const discard = () => (partner === null ? navigate(route('partners.index')) : setForm(toInput(partner)));

  const text = (key: TextField, label: string, type = 'text') => (
    <Field label={label} htmlFor={`c-${key}`}>
      <input id={`c-${key}`} type={type} className="o-input" value={form[key]} onChange={(e) => setForm({ ...form, [key]: e.target.value })} />
    </Field>
  );

  return (
    <>
      <ControlPanel
        breadcrumbs={[{ label: __('Contacts'), to: route('partners.index') }, { label: partner?.name ?? __('New') }]}
        actions={
          <>
            <button className="btn" disabled={!dirty || processing || form.name.trim() === ''} onClick={() => void save()}><Save className="h-4 w-4" />{__('Save')}</button>
            {dirty && <button className="btn-secondary" onClick={discard}><Undo2 className="h-4 w-4" />{__('Discard')}</button>}
          </>
        }
      />
      {partner && (
        <div className="no-print border-b border-gray-200 bg-white px-4 py-2">
          <div className="mx-auto flex max-w-6xl gap-2">
            <button className="btn-secondary" disabled={processing} onClick={() => void save({ ...form, active: !form.active })}>
              {partner.active ? __('Archive') : __('Unarchive')}
            </button>
          </div>
        </div>
      )}
      <Sheet ribbon={partner && !partner.active ? <Ribbon tone="gray">{__('Archived')}</Ribbon> : undefined}>
        <fieldset className="mb-4 flex flex-wrap gap-4">
          <legend className="sr-only">{__('Contact type')}</legend>
          {Object.values(PartnerKind).map((k) => (
            <label key={k} className="flex items-center gap-1.5 text-sm">
              <input type="radio" name="c-type" value={k} checked={form.type === k} onChange={() => setForm({ ...form, type: k })} className="accent-brand" />
              {KIND[k].label}
            </label>
          ))}
        </fieldset>
        <div className="mb-6 flex items-start gap-4">
          <div className="min-w-0 flex-1">
            <label htmlFor="c-name" className="sr-only">{__('Name')}</label>
            <input id="c-name" className="o-input text-3xl font-semibold" placeholder={__('e.g. Lumber Inc')} value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} autoFocus={partner === null} required />
            <label htmlFor="c-contactPerson" className="sr-only">{__('Contact person')}</label>
            <input id="c-contactPerson" className="o-input mt-2" placeholder={__('Contact person')} value={form.contactPerson} onChange={(e) => setForm({ ...form, contactPerson: e.target.value })} />
          </div>
          <Avatar name={form.name || '?'} size="lg" />
        </div>
        <Columns>
          <FieldGroup>
            {text('address', __('Address'))}
            {text('taxId', __('Tax ID'))}
          </FieldGroup>
          <FieldGroup>
            {text('phone', __('Phone'), 'tel')}
            {text('email', __('Email'), 'email')}
          </FieldGroup>
        </Columns>
      </Sheet>
    </>
  );
}
