import { Banknote, PackageCheck, Printer, Trash2 } from 'lucide-react';
import { useCallback, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { date as formatDate, money, quantity, today } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { route } from '../../../lib/routes';
import { useLoad } from '../../../lib/useLoad';
import { Badge, Ribbon } from '../../../ui/Badge';
import { ControlPanel } from '../../../ui/ControlPanel';
import { Dialog } from '../../../ui/Dialog';
import { Columns, Field, FieldGroup, Notebook, Sheet, SheetTitle, SmartButton, SmartButtons, StatusBar, Totals } from '../../../ui/Form';
import { Many2One } from '../../../ui/Many2One';
import { useToast } from '../../../ui/Toast';
import { purchaseApi } from '../api';
import { SupplierPaymentDialog } from '../components/SupplierPaymentDialog';
import { CHANNEL_LABEL, ORDER_STATUS, PAYMENT_STATUS } from '../labels';
import { OrderStatus, PaymentProgress, type ProductOption, type PurchaseOrder } from '../types';

const FLOW = [OrderStatus.DRAFT, OrderStatus.CONFIRMED, OrderStatus.RECEIVED] as const;

const stagesFor = (status: OrderStatus) =>
  [...FLOW, ...(status === OrderStatus.CANCELLED ? [OrderStatus.CANCELLED] : [])].map((s) => ({ key: s, label: ORDER_STATUS[s].label }));

export function PurchaseOrderFormPage() {
  const { id } = useParams();
  return id === undefined ? <NewOrder /> : <ExistingOrder id={Number(id)} />;
}

interface LineDraft {
  key: number;
  productId: number | null;
  quantity: string;
  unitCost: string;
}

let lineKey = 1;
const emptyLine = (): LineDraft => ({ key: lineKey++, productId: null, quantity: '1', unitCost: '' });

function NewOrder() {
  const navigate = useNavigate();
  const toast = useToast();
  const { data: suppliers } = useLoad(purchaseApi.supplierOptions, []);
  const { data: products } = useLoad(purchaseApi.productOptions, []);
  const [supplierId, setSupplierId] = useState<number | null>(null);
  const [orderDate, setOrderDate] = useState(today());
  const [lines, setLines] = useState<LineDraft[]>([emptyLine()]);
  const [processing, setProcessing] = useState(false);

  const product = (id: number | null): ProductOption | undefined => products.find((p) => p.id === id);
  const subtotal = (l: LineDraft) => Number(l.quantity || 0) * Number(l.unitCost || 0);
  const total = lines.reduce((sum, l) => sum + subtotal(l), 0);
  const filled = lines.filter((l) => l.productId !== null);

  const setLine = (key: number, patch: Partial<LineDraft>) => setLines(lines.map((l) => (l.key === key ? { ...l, ...patch } : l)));

  const save = async () => {
    setProcessing(true);
    let created: PurchaseOrder | undefined;
    const ok = await toast.run(async () => {
      created = await purchaseApi.createOrder({
        supplierId: supplierId!,
        date: orderDate,
        items: filled.map((l) => ({ productId: l.productId!, quantity: Number(l.quantity), unitCost: Number(l.unitCost) })),
      });
    });
    setProcessing(false);
    if (ok && created) {
      toast.success(__('Request for quotation :number created.').replace(':number', created.number));
      navigate(route('purchase.orders.show', { id: created.id }), { replace: true });
    }
  };

  const canSave = supplierId !== null && filled.length > 0 && filled.every((l) => Number(l.quantity) > 0 && l.unitCost !== '' && Number(l.unitCost) >= 0) && !processing;

  return (
    <>
      <ControlPanel breadcrumbs={[{ label: __('Purchase orders'), to: route('purchase.orders') }, { label: __('New') }]} />
      <StatusBar stages={stagesFor(OrderStatus.DRAFT)} current={OrderStatus.DRAFT}>
        <button className="btn" onClick={() => void save()} disabled={!canSave}>{__('Save')}</button>
        <button className="btn-secondary" onClick={() => navigate(route('purchase.orders'))}>{__('Discard')}</button>
      </StatusBar>
      <Sheet>
        <SheetTitle overline={__('Request for Quotation')}><span className="text-gray-400">{__('New')}</span></SheetTitle>
        <Columns>
          <FieldGroup>
            <Field label={__('Vendor')} htmlFor="po-vendor">
              <Many2One
                id="po-vendor"
                autoFocus
                placeholder={__('Search a vendor…')}
                options={suppliers.filter((s) => s.supplier && s.active).map((s) => ({ value: s.id, label: s.name }))}
                value={supplierId}
                onChange={setSupplierId}
              />
            </Field>
          </FieldGroup>
          <FieldGroup>
            <Field label={__('Order date')} htmlFor="po-date">
              <input id="po-date" type="date" className="o-input" value={orderDate} onChange={(e) => setOrderDate(e.target.value)} />
            </Field>
          </FieldGroup>
        </Columns>

        <Notebook
          tabs={[{
            key: 'lines',
            label: __('Products'),
            content: (
              <>
                <table className="w-full">
                  <thead className="border-b border-gray-300">
                    <tr>
                      <th className="th w-1/2">{__('Product')}</th>
                      <th className="th text-right">{__('Quantity')}</th>
                      <th className="th text-right">{__('Unit cost')}</th>
                      <th className="th text-right">{__('Subtotal')}</th>
                      <th className="th w-8" />
                    </tr>
                  </thead>
                  <tbody>
                    {lines.map((l) => {
                      const p = product(l.productId);
                      return (
                        <tr key={l.key} className="border-b border-gray-100 align-top">
                          <td className="td">
                            <Many2One
                              aria-label={__('Product')}
                              placeholder={__('Select a product…')}
                              options={products.filter((o) => o.active).map((o) => ({ value: o.id, label: `[${o.code}] ${o.name}`, hint: `${quantity(o.quantity)} ${o.unit.toLowerCase()}` }))}
                              value={l.productId}
                              onChange={(v) => {
                                const picked = product(v);
                                setLine(l.key, { productId: v, unitCost: picked && picked.averageCost > 0 ? String(picked.averageCost) : '' });
                              }}
                            />
                          </td>
                          <td className="td">
                            <div className="flex items-center gap-1">
                              <input aria-label={__('Quantity')} type="number" min="0.001" step="0.001" className="o-input text-right" value={l.quantity} onChange={(e) => setLine(l.key, { quantity: e.target.value })} />
                              {p && <span className="text-xs text-gray-500">{p.unit.toLowerCase()}</span>}
                            </div>
                          </td>
                          <td className="td">
                            <input aria-label={__('Unit cost')} type="number" min="0" step="0.01" className="o-input text-right" placeholder="0.00" value={l.unitCost} onChange={(e) => setLine(l.key, { unitCost: e.target.value })} />
                          </td>
                          <td className="td text-right tabular-nums">{money(subtotal(l))}</td>
                          <td className="td">
                            <button type="button" className="text-gray-400 hover:text-red-600" aria-label={__('Remove line')} onClick={() => setLines(lines.length > 1 ? lines.filter((x) => x.key !== l.key) : [emptyLine()])}>
                              <Trash2 className="h-4 w-4" />
                            </button>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
                <button type="button" className="btn-link mt-2 px-3" onClick={() => setLines([...lines, emptyLine()])}>{__('Add a product')}</button>
                <Totals rows={[{ label: __('Total'), value: money(total), strong: true }]} />
              </>
            ),
          }]}
        />
      </Sheet>
    </>
  );
}

function ExistingOrder({ id }: { id: number }) {
  const toast = useToast();
  const loadOrder = useCallback(() => purchaseApi.order(id), [id]);
  const { data: order, error, reload } = useLoad<PurchaseOrder | null>(loadOrder, null);
  const { data: payments, reload: reloadPayments } = useLoad(purchaseApi.payments, []);
  const { data: suppliers } = useLoad(purchaseApi.supplierOptions, []);
  const [dialog, setDialog] = useState<'receive' | 'pay' | 'cancel' | null>(null);
  const [receiptDate, setReceiptDate] = useState(today());
  const [processing, setProcessing] = useState(false);

  if (error !== null) return <p className="p-6 text-red-600" role="alert">{error}</p>;
  if (order === null) return <p className="p-6 text-gray-500">{__('Loading…')}</p>;

  const related = payments.filter((p) => p.orderId === order.id);

  const act = async (action: () => Promise<unknown>, message: string) => {
    setProcessing(true);
    const ok = await toast.run(action, message);
    setProcessing(false);
    if (ok) {
      setDialog(null);
      await reload();
    }
  };

  const ribbon = order.status === OrderStatus.CANCELLED
    ? <Ribbon tone="gray">{__('Cancelled')}</Ribbon>
    : order.status === OrderStatus.RECEIVED && order.paymentStatus === PaymentProgress.PAID ? <Ribbon tone="green">{__('Paid')}</Ribbon> : undefined;

  return (
    <>
      <ControlPanel breadcrumbs={[{ label: __('Purchase orders'), to: route('purchase.orders') }, { label: order.number }]} />
      <StatusBar stages={stagesFor(order.status)} current={order.status}>
        {order.status === OrderStatus.DRAFT && (
          <button className="btn" disabled={processing} onClick={() => void act(() => purchaseApi.confirm(order.id), __('Order confirmed.'))}>{__('Confirm order')}</button>
        )}
        {order.status === OrderStatus.CONFIRMED && (
          <button className="btn" disabled={processing} onClick={() => setDialog('receive')}><PackageCheck className="h-4 w-4" />{__('Receive products')}</button>
        )}
        {order.status === OrderStatus.RECEIVED && order.outstanding > 0 && (
          <button className="btn" onClick={() => setDialog('pay')}>{__('Register payment')}</button>
        )}
        <button className="btn-secondary" onClick={() => window.print()}><Printer className="h-4 w-4" />{__('Print')}</button>
        {(order.status === OrderStatus.DRAFT || order.status === OrderStatus.CONFIRMED) && (
          <button className="btn-secondary" disabled={processing} onClick={() => setDialog('cancel')}>{__('Cancel')}</button>
        )}
      </StatusBar>
      <Sheet ribbon={ribbon}>
        <SmartButtons>
          <SmartButton icon={<Banknote className="h-5 w-5" />} value={related.length} label={__('Payments')} />
          <SmartButton icon={<PackageCheck className="h-5 w-5" />} value={order.status === OrderStatus.RECEIVED ? 1 : 0} label={__('Receipt')} />
        </SmartButtons>
        <SheetTitle overline={order.status === OrderStatus.DRAFT ? __('Request for Quotation') : __('Purchase Order')}>{order.number}</SheetTitle>
        <Columns>
          <FieldGroup>
            <Field label={__('Vendor')}><span className="font-medium text-accent">{order.supplierName}</span></Field>
            <Field label={__('Buyer')}>{order.createdBy}</Field>
          </FieldGroup>
          <FieldGroup>
            <Field label={__('Order date')}>{formatDate(order.orderDate)}</Field>
            <Field label={__('Received on')}>{order.receivedDate ? formatDate(order.receivedDate) : <span className="text-gray-400">{__('Not received yet')}</span>}</Field>
            {order.status === OrderStatus.RECEIVED && (
              <Field label={__('Payment status')}><Badge tone={PAYMENT_STATUS[order.paymentStatus].tone}>{PAYMENT_STATUS[order.paymentStatus].label}</Badge></Field>
            )}
          </FieldGroup>
        </Columns>

        <Notebook
          tabs={[
            {
              key: 'lines',
              label: __('Products'),
              content: (
                <>
                  <table className="w-full">
                    <thead className="border-b border-gray-300">
                      <tr>
                        <th className="th">{__('Product')}</th>
                        <th className="th text-right">{__('Quantity')}</th>
                        <th className="th text-right">{__('Received')}</th>
                        <th className="th text-right">{__('Unit cost')}</th>
                        <th className="th text-right">{__('Subtotal')}</th>
                      </tr>
                    </thead>
                    <tbody>
                      {order.lines.map((l, i) => (
                        <tr key={i} className="border-b border-gray-100">
                          <td className="td">{l.productName}</td>
                          <td className="td text-right tabular-nums">{quantity(l.quantity)} {l.unit.toLowerCase()}</td>
                          <td className="td text-right tabular-nums">{quantity(order.status === OrderStatus.RECEIVED ? l.quantity : 0)}</td>
                          <td className="td text-right tabular-nums">{money(l.unitCost)}</td>
                          <td className="td text-right tabular-nums">{money(l.lineTotal)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                  <Totals rows={[
                    { label: __('Total'), value: money(order.total), strong: true },
                    ...(order.status === OrderStatus.RECEIVED ? [
                      { label: __('Paid'), value: money(order.paid) },
                      { label: __('Amount due'), value: money(order.outstanding), strong: true },
                    ] : []),
                  ]} />
                </>
              ),
            },
            {
              key: 'payments',
              label: __('Payments'),
              content: related.length === 0 ? (
                <p className="text-gray-500">{__('No payments registered yet.')}</p>
              ) : (
                <table className="w-full">
                  <thead className="border-b border-gray-300">
                    <tr><th className="th">{__('Date')}</th><th className="th">{__('Journal')}</th><th className="th">{__('Memo')}</th><th className="th text-right">{__('Amount')}</th></tr>
                  </thead>
                  <tbody>
                    {related.map((p) => (
                      <tr key={p.id} className="border-b border-gray-100">
                        <td className="td">{formatDate(p.date)}</td><td className="td">{CHANNEL_LABEL[p.method]}</td>
                        <td className="td">{p.note}</td><td className="td text-right tabular-nums">{money(p.amount)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              ),
            },
          ]}
        />
      </Sheet>

      {dialog === 'receive' && (
        <Dialog title={__('Receive products')} onClose={() => setDialog(null)} submitLabel={__('Validate')} submitDisabled={processing}
          onSubmit={() => act(() => purchaseApi.receive(order.id, receiptDate), __('Products received into stock.'))}>
          <p className="text-gray-600">{__('All ordered quantities will be added to stock, and the amount owed to the vendor will be recorded.')}</p>
          <div>
            <label className="label" htmlFor="po-receipt-date">{__('Date received')}</label>
            <input id="po-receipt-date" type="date" className="input" value={receiptDate} onChange={(e) => setReceiptDate(e.target.value)} required />
          </div>
        </Dialog>
      )}
      {dialog === 'cancel' && (
        <Dialog title={__('Cancel order')} onClose={() => setDialog(null)} submitLabel={__('Cancel order')} submitDisabled={processing}
          onSubmit={() => act(() => purchaseApi.cancel(order.id), __('Order cancelled.'))}>
          <p>{__('Cancel :number? This cannot be undone.').replace(':number', order.number)}</p>
        </Dialog>
      )}
      {dialog === 'pay' && (
        <SupplierPaymentDialog
          order={order}
          suppliers={suppliers}
          orders={[order]}
          onClose={() => setDialog(null)}
          onDone={() => {
            setDialog(null);
            void Promise.all([reload(), reloadPayments()]);
          }}
        />
      )}
    </>
  );
}
