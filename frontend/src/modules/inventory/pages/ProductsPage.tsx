import { useState, type FormEvent } from 'react';
import { ApiError } from '../../../lib/http';
import { money, quantity } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { useLoad } from '../../../lib/useLoad';
import { inventoryApi } from '../api';
import { MovementKind, UnitOfMeasure } from '../types';

const UNIT_LABEL: Record<UnitOfMeasure, string> = {
  [UnitOfMeasure.KG]: __('kg'),
  [UnitOfMeasure.PCS]: __('pcs'),
};

const MOVEMENT_LABEL: Record<MovementKind, string> = {
  [MovementKind.ADJUSTMENT]: __('Adjustment'),
  [MovementKind.SALE]: __('Sale'),
};

export function ProductsPage() {
  const { data: products, error: loadError, reload } = useLoad(inventoryApi.products, []);
  const { data: movements, reload: reloadMovements } = useLoad(inventoryApi.movements, []);
  const [product, setProduct] = useState({ code: '', name: '', unit: UnitOfMeasure.KG as UnitOfMeasure, salePrice: '' });
  const [adjust, setAdjust] = useState({ productId: '', quantity: '', unitCost: '', reason: '' });
  const [error, setError] = useState<string | null>(null);

  const refresh = async () => {
    await Promise.all([reload(), reloadMovements()]);
  };

  const run = async (action: () => Promise<unknown>) => {
    setError(null);
    try {
      await action();
      await refresh();
    } catch (e) {
      setError(e instanceof ApiError ? e.message : __('Something went wrong.'));
    }
  };

  const createProduct = (event: FormEvent) => {
    event.preventDefault();
    void run(async () => {
      await inventoryApi.createProduct({ ...product, salePrice: Number(product.salePrice) });
      setProduct({ code: '', name: '', unit: UnitOfMeasure.KG, salePrice: '' });
    });
  };

  const adjustStock = (event: FormEvent) => {
    event.preventDefault();
    void run(async () => {
      await inventoryApi.adjust({
        productId: Number(adjust.productId),
        quantity: Number(adjust.quantity),
        unitCost: adjust.unitCost === '' ? undefined : Number(adjust.unitCost),
        reason: adjust.reason,
      });
      setAdjust({ productId: '', quantity: '', unitCost: '', reason: '' });
    });
  };

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold">{__('Products and stock')}</h1>
      {(error ?? loadError) !== null && <p role="alert" className="text-sm text-red-600">{error ?? loadError}</p>}

      <div className="grid gap-4 lg:grid-cols-2">
        <form onSubmit={createProduct} className="card grid gap-3 sm:grid-cols-2">
          <h2 className="font-medium sm:col-span-2">{__('New product')}</h2>
          <div>
            <label className="label" htmlFor="p-code">{__('Code')}</label>
            <input id="p-code" className="input" value={product.code} onChange={(e) => setProduct({ ...product, code: e.target.value })} required />
          </div>
          <div>
            <label className="label" htmlFor="p-name">{__('Name')}</label>
            <input id="p-name" className="input" value={product.name} onChange={(e) => setProduct({ ...product, name: e.target.value })} required />
          </div>
          <div>
            <label className="label" htmlFor="p-unit">{__('Unit')}</label>
            <select id="p-unit" className="input" value={product.unit} onChange={(e) => setProduct({ ...product, unit: e.target.value as UnitOfMeasure })}>
              {Object.values(UnitOfMeasure).map((u) => (<option key={u} value={u}>{UNIT_LABEL[u]}</option>))}
            </select>
          </div>
          <div>
            <label className="label" htmlFor="p-price">{__('Sale price')}</label>
            <input id="p-price" type="number" min="0" step="0.01" className="input" value={product.salePrice} onChange={(e) => setProduct({ ...product, salePrice: e.target.value })} required />
          </div>
          <button className="btn sm:col-span-2">{__('Add product')}</button>
        </form>

        <form onSubmit={adjustStock} className="card grid gap-3 sm:grid-cols-2">
          <h2 className="font-medium sm:col-span-2">{__('Stock adjustment')}</h2>
          <div className="sm:col-span-2">
            <label className="label" htmlFor="s-product">{__('Product')}</label>
            <select id="s-product" className="input" value={adjust.productId} onChange={(e) => setAdjust({ ...adjust, productId: e.target.value })} required>
              <option value="">{__('Select product…')}</option>
              {products.map((p) => (<option key={p.id} value={p.id}>{p.code} · {p.name}</option>))}
            </select>
          </div>
          <div>
            <label className="label" htmlFor="s-qty">{__('Quantity (+ add / − remove)')}</label>
            <input id="s-qty" type="number" step="0.001" className="input" value={adjust.quantity} onChange={(e) => setAdjust({ ...adjust, quantity: e.target.value })} required />
          </div>
          <div>
            <label className="label" htmlFor="s-cost">{__('Unit cost (when adding)')}</label>
            <input id="s-cost" type="number" min="0" step="0.01" className="input" value={adjust.unitCost} onChange={(e) => setAdjust({ ...adjust, unitCost: e.target.value })} />
          </div>
          <div className="sm:col-span-2">
            <label className="label" htmlFor="s-reason">{__('Reason')}</label>
            <input id="s-reason" className="input" value={adjust.reason} onChange={(e) => setAdjust({ ...adjust, reason: e.target.value })} required />
          </div>
          <button className="btn sm:col-span-2">{__('Apply adjustment')}</button>
        </form>
      </div>

      <div className="card overflow-x-auto p-0">
        <table className="min-w-full divide-y divide-slate-200">
          <thead>
            <tr>
              <th className="th">{__('Code')}</th><th className="th">{__('Name')}</th><th className="th">{__('Unit')}</th>
              <th className="th text-right">{__('In stock')}</th><th className="th text-right">{__('Avg cost')}</th>
              <th className="th text-right">{__('Stock value')}</th><th className="th text-right">{__('Sale price')}</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {products.map((p) => (
              <tr key={p.id}>
                <td className="td font-mono">{p.code}</td><td className="td">{p.name}</td><td className="td">{UNIT_LABEL[p.unit]}</td>
                <td className="td text-right">{quantity(p.quantity)}</td><td className="td text-right">{money(p.averageCost)}</td>
                <td className="td text-right">{money(p.stockValue)}</td><td className="td text-right">{money(p.salePrice)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <h2 className="font-medium">{__('Stock movements')}</h2>
      <div className="card overflow-x-auto p-0">
        <table className="min-w-full divide-y divide-slate-200">
          <thead>
            <tr>
              <th className="th">{__('Date')}</th><th className="th">{__('Product')}</th><th className="th">{__('Type')}</th>
              <th className="th text-right">{__('Quantity')}</th><th className="th text-right">{__('Unit cost')}</th>
              <th className="th">{__('Reference')}</th><th className="th">{__('Reason')}</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {movements.map((m) => (
              <tr key={m.id}>
                <td className="td">{m.date}</td><td className="td">{m.productName}</td><td className="td">{MOVEMENT_LABEL[m.type]}</td>
                <td className="td text-right">{quantity(m.quantity)}</td><td className="td text-right">{money(m.unitCost)}</td>
                <td className="td">{m.reference}</td><td className="td">{m.reason}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
