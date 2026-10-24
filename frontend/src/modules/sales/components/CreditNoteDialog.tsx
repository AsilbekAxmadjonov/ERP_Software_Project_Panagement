import { useState } from 'react';
import { money, quantity, today } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { Dialog } from '../../../ui/Dialog';
import { useToast } from '../../../ui/Toast';
import { salesApi } from '../api';
import type { Invoice } from '../types';

interface Props {
  invoice: Invoice;
  onClose: () => void;
  onDone: () => void;
}

export function CreditNoteDialog({ invoice, onClose, onDone }: Props) {
  const toast = useToast();
  const lines = invoice.lines;
  const [quantities, setQuantities] = useState<Record<number, string>>({});
  const [reason, setReason] = useState('');
  const [noteDate, setNoteDate] = useState(today());
  const [processing, setProcessing] = useState(false);

  const qty = (productId: number) => Number(quantities[productId] || 0);
  const total = lines.reduce((sum, l) => sum + qty(l.productId) * l.unitPrice, 0);
  const valid = total > 0 && lines.every((l) => qty(l.productId) <= l.returnable + 1e-9) && reason.trim() !== '';

  const submit = async () => {
    setProcessing(true);
    const ok = await toast.run(
      () => salesApi.issueCreditNote(invoice.id, {
        date: noteDate,
        reason,
        items: lines.filter((l) => qty(l.productId) > 0).map((l) => ({ productId: l.productId, quantity: qty(l.productId) })),
      }),
      __('Credit note created. Returned goods are back in stock.'),
    );
    setProcessing(false);
    if (ok) onDone();
  };

  return (
    <Dialog title={__('Credit note for :number').replace(':number', invoice.number)} size="lg" onClose={onClose} onSubmit={submit} submitLabel={__('Create credit note')} submitDisabled={!valid || processing}>
      <div className="grid gap-3 sm:grid-cols-2">
        <div>
          <label className="label" htmlFor="cn-reason">{__('Reason')}</label>
          <input id="cn-reason" className="input" placeholder={__('e.g. damaged goods')} value={reason} onChange={(e) => setReason(e.target.value)} required autoFocus />
        </div>
        <div>
          <label className="label" htmlFor="cn-date">{__('Date')}</label>
          <input id="cn-date" type="date" className="input" value={noteDate} onChange={(e) => setNoteDate(e.target.value)} required />
        </div>
      </div>
      <table className="w-full">
        <thead className="border-b border-gray-300">
          <tr>
            <th className="th">{__('Product')}</th>
            <th className="th text-right">{__('Returnable')}</th>
            <th className="th w-32 text-right">{__('Return qty')}</th>
            <th className="th text-right">{__('Unit price')}</th>
            <th className="th text-right">{__('Subtotal')}</th>
          </tr>
        </thead>
        <tbody>
          {lines.map((l) => {
            const over = qty(l.productId) > l.returnable + 1e-9;
            return (
              <tr key={l.productId} className="border-b border-gray-100">
                <td className="td">{l.productName}</td>
                <td className="td text-right tabular-nums">{quantity(l.returnable)} {l.unit.toLowerCase()}</td>
                <td className="td">
                  <input
                    aria-label={__('Return quantity for :product').replace(':product', l.productName)}
                    type="number" min="0" max={l.returnable} step="0.001"
                    className={`input text-right ${over ? 'border-red-400 text-red-600' : ''}`}
                    value={quantities[l.productId] ?? ''}
                    disabled={l.returnable === 0}
                    onChange={(e) => setQuantities({ ...quantities, [l.productId]: e.target.value })}
                  />
                </td>
                <td className="td text-right tabular-nums">{money(l.unitPrice)}</td>
                <td className="td text-right tabular-nums">{money(qty(l.productId) * l.unitPrice)}</td>
              </tr>
            );
          })}
        </tbody>
        <tfoot>
          <tr className="font-semibold">
            <td className="td" colSpan={4}>{__('Total to credit')}</td>
            <td className="td text-right tabular-nums">{money(total)}</td>
          </tr>
        </tfoot>
      </table>
    </Dialog>
  );
}
