import { useState } from 'react';
import { money, today } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { Dialog } from '../../../ui/Dialog';
import { Many2One } from '../../../ui/Many2One';
import { useToast } from '../../../ui/Toast';
import { salesApi } from '../api';
import { CHANNEL_LABEL } from '../labels';
import { PaymentChannel, type CustomerOption, type Invoice } from '../types';

interface Props {
  /** Pre-selects the invoice and its customer when registering from an invoice. */
  invoice?: Invoice;
  customers: readonly CustomerOption[];
  invoices: readonly Invoice[];
  onClose: () => void;
  onDone: () => void;
}

export function RegisterPaymentDialog({ invoice, customers, invoices, onClose, onDone }: Props) {
  const toast = useToast();
  const [customerId, setCustomerId] = useState<number | null>(invoice?.customerId ?? null);
  const [invoiceId, setInvoiceId] = useState<number | null>(invoice?.id ?? null);
  const [amount, setAmount] = useState(invoice ? String(invoice.outstanding) : '');
  const [method, setMethod] = useState<PaymentChannel>(PaymentChannel.CASH);
  const [paymentDate, setPaymentDate] = useState(today());
  const [note, setNote] = useState('');
  const [processing, setProcessing] = useState(false);

  const openInvoices = invoices.filter((i) => i.customerId === customerId && i.outstanding > 0);

  const submit = async () => {
    setProcessing(true);
    const ok = await toast.run(
      () => salesApi.recordPayment({ customerId: customerId!, invoiceId: invoiceId ?? undefined, amount: Number(amount), method, date: paymentDate, note }),
      __('Payment registered.'),
    );
    setProcessing(false);
    if (ok) onDone();
  };

  return (
    <Dialog title={__('Register payment')} onClose={onClose} onSubmit={submit} submitLabel={__('Create payment')} submitDisabled={processing || customerId === null}>
      <div className="grid gap-3 sm:grid-cols-2">
        <div className="sm:col-span-2">
          <label className="label" htmlFor="pay-customer">{__('Customer')}</label>
          <Many2One
            id="pay-customer"
            variant="boxed"
            options={customers.filter((c) => c.customer && c.active).map((c) => ({ value: c.id, label: c.name }))}
            value={customerId}
            onChange={(v) => {
              setCustomerId(v);
              setInvoiceId(null);
            }}
            disabled={invoice !== undefined}
            required
          />
        </div>
        <div className="sm:col-span-2">
          <label className="label" htmlFor="pay-invoice">{__('Invoice')}</label>
          <select
            id="pay-invoice"
            className="input"
            value={invoiceId ?? ''}
            disabled={invoice !== undefined}
            onChange={(e) => {
              const id = e.target.value === '' ? null : Number(e.target.value);
              setInvoiceId(id);
              const picked = openInvoices.find((i) => i.id === id);
              if (picked) setAmount(String(picked.outstanding));
            }}
          >
            <option value="">{__('No invoice (advance payment)')}</option>
            {(invoice ? [invoice] : openInvoices).map((i) => (
              <option key={i.id} value={i.id}>{i.number} · {__('due')} {money(i.outstanding)}</option>
            ))}
          </select>
        </div>
        <div>
          <label className="label" htmlFor="pay-amount">{__('Amount')}</label>
          <input id="pay-amount" type="number" min="0.01" step="0.01" className="input text-right" value={amount} onChange={(e) => setAmount(e.target.value)} required autoFocus />
        </div>
        <div>
          <label className="label" htmlFor="pay-date">{__('Payment date')}</label>
          <input id="pay-date" type="date" className="input" value={paymentDate} onChange={(e) => setPaymentDate(e.target.value)} required />
        </div>
        <fieldset className="sm:col-span-2">
          <legend className="label">{__('Journal')}</legend>
          <div className="flex gap-4">
            {Object.values(PaymentChannel).map((m) => (
              <label key={m} className="flex items-center gap-1.5">
                <input type="radio" name="pay-method" value={m} checked={method === m} onChange={() => setMethod(m)} className="accent-brand" />
                {CHANNEL_LABEL[m]}
              </label>
            ))}
          </div>
        </fieldset>
        <div className="sm:col-span-2">
          <label className="label" htmlFor="pay-note">{__('Memo')}</label>
          <input id="pay-note" className="input" value={note} onChange={(e) => setNote(e.target.value)} />
        </div>
      </div>
    </Dialog>
  );
}
