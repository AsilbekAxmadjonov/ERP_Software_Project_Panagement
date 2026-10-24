import { useState } from 'react';
import { money, today } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { Dialog } from '../../../ui/Dialog';
import { Many2One } from '../../../ui/Many2One';
import { useToast } from '../../../ui/Toast';
import { purchaseApi } from '../api';
import { CHANNEL_LABEL } from '../labels';
import { PaymentChannel, type PurchaseOrder, type SupplierOption } from '../types';

interface Props {
  /** Pre-selects the order and its vendor when paying from an order. */
  order?: PurchaseOrder;
  suppliers: readonly SupplierOption[];
  orders: readonly PurchaseOrder[];
  onClose: () => void;
  onDone: () => void;
}

export function SupplierPaymentDialog({ order, suppliers, orders, onClose, onDone }: Props) {
  const toast = useToast();
  const [supplierId, setSupplierId] = useState<number | null>(order?.supplierId ?? null);
  const [orderId, setOrderId] = useState<number | null>(order?.id ?? null);
  const [amount, setAmount] = useState(order ? String(order.outstanding) : '');
  const [method, setMethod] = useState<PaymentChannel>(PaymentChannel.BANK);
  const [paymentDate, setPaymentDate] = useState(today());
  const [note, setNote] = useState('');
  const [processing, setProcessing] = useState(false);

  const openOrders = order ? [order] : orders.filter((o) => o.supplierId === supplierId && o.outstanding > 0);

  const submit = async () => {
    setProcessing(true);
    const ok = await toast.run(
      () => purchaseApi.recordPayment({ supplierId: supplierId!, orderId: orderId ?? undefined, amount: Number(amount), method, date: paymentDate, note }),
      __('Vendor payment registered.'),
    );
    setProcessing(false);
    if (ok) onDone();
  };

  return (
    <Dialog title={__('Pay vendor')} onClose={onClose} onSubmit={submit} submitLabel={__('Create payment')} submitDisabled={processing || supplierId === null}>
      <div className="grid gap-3 sm:grid-cols-2">
        <div className="sm:col-span-2">
          <label className="label" htmlFor="sp-supplier">{__('Vendor')}</label>
          <Many2One
            id="sp-supplier"
            variant="boxed"
            options={suppliers.filter((s) => s.supplier && s.active).map((s) => ({ value: s.id, label: s.name }))}
            value={supplierId}
            onChange={(v) => {
              setSupplierId(v);
              setOrderId(null);
            }}
            disabled={order !== undefined}
            required
          />
        </div>
        <div className="sm:col-span-2">
          <label className="label" htmlFor="sp-order">{__('Purchase order')}</label>
          <select
            id="sp-order"
            className="input"
            value={orderId ?? ''}
            disabled={order !== undefined}
            onChange={(e) => {
              const id = e.target.value === '' ? null : Number(e.target.value);
              setOrderId(id);
              const picked = openOrders.find((o) => o.id === id);
              if (picked) setAmount(String(picked.outstanding));
            }}
          >
            <option value="">{__('No order (advance to vendor)')}</option>
            {openOrders.map((o) => (
              <option key={o.id} value={o.id}>{o.number} · {__('due')} {money(o.outstanding)}</option>
            ))}
          </select>
        </div>
        <div>
          <label className="label" htmlFor="sp-amount">{__('Amount')}</label>
          <input id="sp-amount" type="number" min="0.01" step="0.01" className="input text-right" value={amount} onChange={(e) => setAmount(e.target.value)} required autoFocus />
        </div>
        <div>
          <label className="label" htmlFor="sp-date">{__('Payment date')}</label>
          <input id="sp-date" type="date" className="input" value={paymentDate} onChange={(e) => setPaymentDate(e.target.value)} required />
        </div>
        <fieldset className="sm:col-span-2">
          <legend className="label">{__('Journal')}</legend>
          <div className="flex gap-4">
            {Object.values(PaymentChannel).map((m) => (
              <label key={m} className="flex items-center gap-1.5">
                <input type="radio" name="sp-method" value={m} checked={method === m} onChange={() => setMethod(m)} className="accent-brand" />
                {CHANNEL_LABEL[m]}
              </label>
            ))}
          </div>
        </fieldset>
        <div className="sm:col-span-2">
          <label className="label" htmlFor="sp-note">{__('Memo')}</label>
          <input id="sp-note" className="input" value={note} onChange={(e) => setNote(e.target.value)} />
        </div>
      </div>
    </Dialog>
  );
}
