import { ArrowRightLeft, Boxes, Save, Undo2 } from 'lucide-react';
import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { date, money, quantity } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { route } from '../../../lib/routes';
import { useLoad } from '../../../lib/useLoad';
import { Badge, Ribbon } from '../../../ui/Badge';
import { ControlPanel } from '../../../ui/ControlPanel';
import { Columns, Field, FieldGroup, Notebook, Sheet, SmartButton, SmartButtons } from '../../../ui/Form';
import { useToast } from '../../../ui/Toast';
import { inventoryApi } from '../api';
import { AdjustStockDialog } from '../components/AdjustStockDialog';
import { MOVEMENT, UNIT_LABEL } from '../labels';
import { UnitOfMeasure, type Product, type ProductInput, type StockMovement } from '../types';

interface Draft {
  code: string;
  name: string;
  unit: UnitOfMeasure;
  salePrice: string;
  reorderLevel: string;
  active: boolean;
}

const EMPTY: Draft = { code: '', name: '', unit: UnitOfMeasure.PCS, salePrice: '', reorderLevel: '', active: true };

const toDraft = (p: Product): Draft => ({
  code: p.code,
  name: p.name,
  unit: p.unit,
  salePrice: String(p.salePrice),
  reorderLevel: p.reorderLevel === null ? '' : String(p.reorderLevel),
  active: p.active,
});

const toInput = (d: Draft): ProductInput => ({
  code: d.code,
  name: d.name,
  unit: d.unit,
  salePrice: Number(d.salePrice),
  active: d.active,
  reorderLevel: d.reorderLevel === '' ? null : Number(d.reorderLevel),
});

export function ProductFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const toast = useToast();
  const productId = id === undefined ? null : Number(id);

  const loadProduct = useCallback(() => (productId === null ? Promise.resolve(null) : inventoryApi.product(productId)), [productId]);
  const { data: product, error, reload } = useLoad<Product | null>(loadProduct, null);
  const loadMoves = useCallback(() => (productId === null ? Promise.resolve<StockMovement[]>([]) : inventoryApi.movements(productId)), [productId]);
  const { data: moves, reload: reloadMoves } = useLoad(loadMoves, []);

  const [draft, setDraft] = useState<Draft>(EMPTY);
  const [processing, setProcessing] = useState(false);
  const [adjusting, setAdjusting] = useState(false);

  useEffect(() => {
    if (product) setDraft(toDraft(product));
  }, [product]);

  if (error !== null) return <p className="p-6 text-red-600" role="alert">{error}</p>;
  if (productId !== null && product === null) return <p className="p-6 text-gray-500">{__('Loading…')}</p>;

  const dirty = product === null || JSON.stringify(toDraft(product)) !== JSON.stringify(draft);
  const set = (patch: Partial<Draft>) => setDraft({ ...draft, ...patch });

  const save = async (next: Draft = draft) => {
    setProcessing(true);
    let saved: Product | undefined;
    const ok = await toast.run(async () => {
      saved = product === null ? await inventoryApi.createProduct(toInput(next)) : await inventoryApi.updateProduct(product.id, toInput(next));
    }, __('Product saved.'));
    setProcessing(false);
    if (!ok || !saved) return;
    if (product === null) navigate(route('inventory.products.show', { id: saved.id }), { replace: true });
    else await reload();
  };

  const discard = () => (product === null ? navigate(route('inventory.products')) : setDraft(toDraft(product)));

  return (
    <>
      <ControlPanel
        breadcrumbs={[{ label: __('Products'), to: route('inventory.products') }, { label: product?.name ?? __('New') }]}
        actions={
          <>
            <button className="btn" disabled={!dirty || processing} onClick={() => void save()}><Save className="h-4 w-4" />{__('Save')}</button>
            {dirty && <button className="btn-secondary" onClick={discard}><Undo2 className="h-4 w-4" />{__('Discard')}</button>}
          </>
        }
      />
      {product && (
        <div className="no-print border-b border-gray-200 bg-white px-4 py-2">
          <div className="mx-auto flex max-w-6xl flex-wrap gap-2">
            <button className="btn-secondary" onClick={() => setAdjusting(true)} disabled={!product.active}>{__('Update quantity')}</button>
            <button className="btn-secondary" onClick={() => void save({ ...draft, active: !draft.active })} disabled={processing}>
              {product.active ? __('Archive') : __('Unarchive')}
            </button>
          </div>
        </div>
      )}
      <Sheet ribbon={product && !product.active ? <Ribbon tone="gray">{__('Archived')}</Ribbon> : undefined}>
        {product && (
          <SmartButtons>
            <SmartButton icon={<Boxes className="h-5 w-5" />} value={`${quantity(product.quantity)} ${UNIT_LABEL[product.unit]}`} label={__('On hand')} />
            <SmartButton icon={<ArrowRightLeft className="h-5 w-5" />} value={moves.length} label={__('In / Out')} onClick={() => navigate(route('inventory.moves'))} />
          </SmartButtons>
        )}
        <div className="mb-6">
          <label htmlFor="prod-name" className="text-sm text-gray-500">{__('Product name')}</label>
          <input id="prod-name" className="o-input text-3xl font-semibold" placeholder={__('e.g. Cheese burger')} value={draft.name} onChange={(e) => set({ name: e.target.value })} autoFocus={product === null} required />
          {product?.lowStock && <div className="mt-2"><Badge tone="yellow">{__('Below minimum quantity')}</Badge></div>}
        </div>
        <Notebook
          tabs={[
            {
              key: 'general',
              label: __('General information'),
              content: (
                <Columns>
                  <FieldGroup>
                    <Field label={__('Internal reference')} htmlFor="prod-code">
                      <input id="prod-code" className="o-input font-mono" value={draft.code} onChange={(e) => set({ code: e.target.value })} required />
                    </Field>
                    <Field label={__('Unit of measure')} htmlFor="prod-unit">
                      <select id="prod-unit" className="o-input" value={draft.unit} onChange={(e) => set({ unit: e.target.value as UnitOfMeasure })}>
                        {Object.values(UnitOfMeasure).map((u) => (<option key={u} value={u}>{UNIT_LABEL[u]}</option>))}
                      </select>
                    </Field>
                    <Field label={__('Sales price')} htmlFor="prod-price">
                      <input id="prod-price" type="number" min="0" step="0.01" className="o-input" value={draft.salePrice} onChange={(e) => set({ salePrice: e.target.value })} required />
                    </Field>
                    <Field label={__('Min. quantity')} htmlFor="prod-min">
                      <input id="prod-min" type="number" min="0" step="0.001" className="o-input" placeholder={__('No alert')} value={draft.reorderLevel} onChange={(e) => set({ reorderLevel: e.target.value })} />
                    </Field>
                  </FieldGroup>
                  {product && (
                    <FieldGroup>
                      <Field label={__('On hand')}><span className="tabular-nums">{quantity(product.quantity)} {UNIT_LABEL[product.unit]}</span></Field>
                      <Field label={__('Average cost')}><span className="tabular-nums">{money(product.averageCost)}</span></Field>
                      <Field label={__('Stock value')}><span className="tabular-nums">{money(product.stockValue)}</span></Field>
                    </FieldGroup>
                  )}
                </Columns>
              ),
            },
            ...(product
              ? [{
                  key: 'moves',
                  label: __('Stock moves'),
                  content: moves.length === 0 ? (
                    <p className="text-gray-500">{__('No stock moves yet.')}</p>
                  ) : (
                    <table className="w-full">
                      <thead className="border-b border-gray-300">
                        <tr>
                          <th className="th">{__('Date')}</th><th className="th">{__('Type')}</th><th className="th">{__('Reference')}</th>
                          <th className="th text-right">{__('Quantity')}</th><th className="th text-right">{__('Unit cost')}</th>
                        </tr>
                      </thead>
                      <tbody>
                        {moves.map((m) => (
                          <tr key={m.id} className="border-b border-gray-100">
                            <td className="td">{date(m.date)}</td>
                            <td className="td"><Badge tone={MOVEMENT[m.type].tone}>{MOVEMENT[m.type].label}</Badge></td>
                            <td className="td">{m.reference ?? m.reason}</td>
                            <td className={`td text-right tabular-nums ${m.quantity < 0 ? 'text-red-600' : 'text-emerald-700'}`}>{m.quantity > 0 ? '+' : ''}{quantity(m.quantity)}</td>
                            <td className="td text-right tabular-nums">{money(m.unitCost)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  ),
                }]
              : []),
          ]}
        />
      </Sheet>
      {adjusting && product && (
        <AdjustStockDialog
          product={product}
          onClose={() => setAdjusting(false)}
          onDone={() => {
            setAdjusting(false);
            void Promise.all([reload(), reloadMoves()]);
          }}
        />
      )}
    </>
  );
}
