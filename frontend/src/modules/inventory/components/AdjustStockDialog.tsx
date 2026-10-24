import { useState } from 'react';
import { money, quantity } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { Dialog } from '../../../ui/Dialog';
import { useToast } from '../../../ui/Toast';
import { inventoryApi } from '../api';
import { UNIT_LABEL } from '../labels';
import type { Product } from '../types';

/** Odoo-style "Update quantity": the user enters the counted quantity and the difference is adjusted. */
export function AdjustStockDialog({ product, onClose, onDone }: { product: Product; onClose: () => void; onDone: () => void }) {
  const toast = useToast();
  const [counted, setCounted] = useState(String(product.quantity));
  const [unitCost, setUnitCost] = useState('');
  const [reason, setReason] = useState('');
  const [processing, setProcessing] = useState(false);

  const difference = Number(counted || 0) - product.quantity;
  const adding = difference > 0;

  const submit = async () => {
    setProcessing(true);
    const ok = await toast.run(
      () => inventoryApi.adjust({ productId: product.id, quantity: difference, unitCost: adding && unitCost !== '' ? Number(unitCost) : undefined, reason }),
      __('Stock updated.'),
    );
    setProcessing(false);
    if (ok) onDone();
  };

  return (
    <Dialog title={__('Update quantity: :name').replace(':name', product.name)} onClose={onClose} onSubmit={submit} submitLabel={__('Apply')} submitDisabled={processing || Math.abs(difference) < 0.0005 || reason.trim() === ''}>
      <div className="grid gap-3 sm:grid-cols-2">
        <div>
          <span className="label">{__('On hand')}</span>
          <p className="py-1.5 tabular-nums">{quantity(product.quantity)} {UNIT_LABEL[product.unit]}</p>
        </div>
        <div>
          <label className="label" htmlFor="adj-counted">{__('Counted quantity')}</label>
          <input id="adj-counted" type="number" min="0" step="0.001" className="input text-right" value={counted} onChange={(e) => setCounted(e.target.value)} required autoFocus />
        </div>
        <p className={`text-sm sm:col-span-2 ${difference < 0 ? 'text-red-600' : 'text-emerald-700'}`}>
          {__('Difference')}: {difference > 0 ? '+' : ''}{quantity(difference)} {UNIT_LABEL[product.unit]}
        </p>
        {adding && (
          <div className="sm:col-span-2">
            <label className="label" htmlFor="adj-cost">{__('Unit cost of added stock')}</label>
            <input id="adj-cost" type="number" min="0" step="0.01" className="input text-right" placeholder={money(product.averageCost)} value={unitCost} onChange={(e) => setUnitCost(e.target.value)} />
          </div>
        )}
        <div className="sm:col-span-2">
          <label className="label" htmlFor="adj-reason">{__('Reason')}</label>
          <input id="adj-reason" className="input" placeholder={__('e.g. Inventory count, damaged goods')} value={reason} onChange={(e) => setReason(e.target.value)} required />
        </div>
      </div>
    </Dialog>
  );
}
