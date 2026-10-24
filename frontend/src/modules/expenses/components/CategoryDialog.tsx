import { useCallback, useState } from 'react';
import { __ } from '../../../lib/i18n';
import { useLoad } from '../../../lib/useLoad';
import { Dialog } from '../../../ui/Dialog';
import { useToast } from '../../../ui/Toast';
import { expensesApi } from '../api';
import { TYPE } from '../labels';
import { EntryType, type Category } from '../types';

interface Props {
  /** Edits this category; creates a new one when absent. */
  category?: Category;
  onClose: () => void;
  onDone: () => void;
}

export function CategoryDialog({ category, onClose, onDone }: Props) {
  const toast = useToast();
  const [name, setName] = useState(category?.name ?? '');
  const [type, setType] = useState<EntryType>(category?.type ?? EntryType.EXPENSE);
  const [accountCode, setAccountCode] = useState(category?.accountCode ?? '');
  const [active, setActive] = useState(category?.active ?? true);
  const [processing, setProcessing] = useState(false);
  const loadAccounts = useCallback(() => expensesApi.accounts(type), [type]);
  const { data: accounts } = useLoad(loadAccounts, []);

  const submit = async () => {
    setProcessing(true);
    const ok = await toast.run(
      () => (category ? expensesApi.updateCategory(category.id, { name, accountCode, active }) : expensesApi.createCategory({ name, type, accountCode })),
      __('Category saved.'),
    );
    setProcessing(false);
    if (ok) onDone();
  };

  return (
    <Dialog title={category ? category.name : __('New category')} onClose={onClose} onSubmit={submit} submitLabel={__('Save')} submitDisabled={processing || name.trim() === '' || accountCode === ''}>
      <div>
        <label className="label" htmlFor="cat-name">{__('Name')}</label>
        <input id="cat-name" className="input" value={name} onChange={(e) => setName(e.target.value)} placeholder={__('e.g. Marketing')} required autoFocus />
      </div>
      <fieldset>
        <legend className="label">{__('Type')}</legend>
        <div className="flex gap-4">
          {Object.values(EntryType).map((t) => (
            <label key={t} className="flex items-center gap-1.5">
              <input type="radio" name="cat-type" value={t} checked={type === t} disabled={category !== undefined} className="accent-brand"
                onChange={() => {
                  setType(t);
                  setAccountCode('');
                }} />
              {TYPE[t].label}
            </label>
          ))}
        </div>
      </fieldset>
      <div>
        <label className="label" htmlFor="cat-account">{__('Ledger account')}</label>
        <select id="cat-account" className="input" value={accountCode} onChange={(e) => setAccountCode(e.target.value)} required>
          <option value="">{__('Select an account…')}</option>
          {accounts.map((a) => (
            <option key={a.code} value={a.code}>{a.code} {a.name}</option>
          ))}
        </select>
      </div>
      {category && (
        <label className="flex items-center gap-2">
          <input type="checkbox" checked={active} onChange={(e) => setActive(e.target.checked)} className="accent-brand" />
          {__('Active')}
        </label>
      )}
    </Dialog>
  );
}
