import { useState } from 'react';
import { today } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { Dialog } from '../../../ui/Dialog';
import { Many2One } from '../../../ui/Many2One';
import { useToast } from '../../../ui/Toast';
import { expensesApi } from '../api';
import { CHANNEL_LABEL, TYPE } from '../labels';
import { PaymentChannel, type CashEntry, type Category, type EntryType } from '../types';

interface Props {
  type: EntryType;
  categories: readonly Category[];
  onClose: () => void;
  onDone: (entry: CashEntry) => void;
}

export function CashEntryDialog({ type, categories, onClose, onDone }: Props) {
  const toast = useToast();
  const [categoryId, setCategoryId] = useState<number | null>(null);
  const [entryDate, setEntryDate] = useState(today());
  const [amount, setAmount] = useState('');
  const [method, setMethod] = useState<PaymentChannel>(PaymentChannel.CASH);
  const [description, setDescription] = useState('');
  const [processing, setProcessing] = useState(false);

  const options = categories.filter((c) => c.type === type && c.active).map((c) => ({ value: c.id, label: c.name, hint: c.accountCode }));

  const submit = async () => {
    setProcessing(true);
    let saved: CashEntry | undefined;
    const ok = await toast.run(async () => {
      saved = await expensesApi.record({ type, categoryId: categoryId!, date: entryDate, amount: Number(amount), method, description });
    }, TYPE[type].recorded);
    setProcessing(false);
    if (ok && saved) onDone(saved);
  };

  return (
    <Dialog title={TYPE[type].newTitle} onClose={onClose} onSubmit={submit} submitLabel={__('Save')} submitDisabled={processing || categoryId === null || !(Number(amount) > 0)}>
      <div className="grid gap-3 sm:grid-cols-2">
        <div className="sm:col-span-2">
          <label className="label" htmlFor="ce-category">{__('Category')}</label>
          <Many2One id="ce-category" variant="boxed" autoFocus options={options} value={categoryId} onChange={setCategoryId} placeholder={__('Select a category…')} required />
        </div>
        <div>
          <label className="label" htmlFor="ce-amount">{__('Amount')}</label>
          <input id="ce-amount" type="number" min="0.01" step="0.01" className="input text-right" value={amount} onChange={(e) => setAmount(e.target.value)} required />
        </div>
        <div>
          <label className="label" htmlFor="ce-date">{__('Date')}</label>
          <input id="ce-date" type="date" className="input" value={entryDate} onChange={(e) => setEntryDate(e.target.value)} required />
        </div>
        <fieldset className="sm:col-span-2">
          <legend className="label">{__('Journal')}</legend>
          <div className="flex gap-4">
            {Object.values(PaymentChannel).map((m) => (
              <label key={m} className="flex items-center gap-1.5">
                <input type="radio" name="ce-method" value={m} checked={method === m} onChange={() => setMethod(m)} className="accent-brand" />
                {CHANNEL_LABEL[m]}
              </label>
            ))}
          </div>
        </fieldset>
        <div className="sm:col-span-2">
          <label className="label" htmlFor="ce-description">{__('Description')}</label>
          <input id="ce-description" className="input" value={description} onChange={(e) => setDescription(e.target.value)} />
        </div>
      </div>
    </Dialog>
  );
}
