import { Banknote, Printer, Trash2, Undo2 } from 'lucide-react';
import { useCallback, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { date as formatDate, money, quantity, today } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { route } from '../../../lib/routes';
import { useLoad } from '../../../lib/useLoad';
import { Badge, Ribbon, type Tone } from '../../../ui/Badge';
import { ControlPanel } from '../../../ui/ControlPanel';
import { Columns, Field, FieldGroup, Notebook, Sheet, SheetTitle, SmartButton, SmartButtons, StatusBar, Totals } from '../../../ui/Form';
import { Many2One } from '../../../ui/Many2One';
import { useToast } from '../../../ui/Toast';
import { salesApi } from '../api';
import { CreditNoteDialog } from '../components/CreditNoteDialog';
import { RegisterPaymentDialog } from '../components/RegisterPaymentDialog';
import { CHANNEL_LABEL, STATUS } from '../labels';
import { PaymentProgress, type Invoice, type ProductOption } from '../types';

type Stage = 'draft' | 'posted';

const RIBBON: Record<PaymentProgress, { label: string; tone: Tone } | null> = {
  [PaymentProgress.UNPAID]: null,
  [PaymentProgress.PARTIAL]: { label: __('Partial'), tone: 'yellow' },
  [PaymentProgress.PAID]: { label: __('Paid'), tone: 'green' },
  [PaymentProgress.CREDITED]: { label: __('Credited'), tone: 'gray' },
};

const STAGES: readonly { key: Stage; label: string }[] = [
  { key: 'draft', label: __('Draft') },
  { key: 'posted', label: __('Posted') },
];

export function InvoiceFormPage() {
  const { id } = useParams();
  return id === undefined ? <NewInvoice /> : <ExistingInvoice id={Number(id)} />;
}

interface LineDraft {
  key: number;
  productId: number | null;
  quantity: string;
  unitPrice: string;
}

let lineKey = 1;
const emptyLine = (): LineDraft => ({ key: lineKey++, productId: null, quantity: '1', unitPrice: '' });

function NewInvoice() {
  const navigate = useNavigate();
  const toast = useToast();
  const { data: customers } = useLoad(salesApi.customerOptions, []);
  const { data: products } = useLoad(salesApi.productOptions, []);
  const [customerId, setCustomerId] = useState<number | null>(null);
  const [invoiceDate, setInvoiceDate] = useState(today());
  const [lines, setLines] = useState<LineDraft[]>([emptyLine()]);
  const [processing, setProcessing] = useState(false);

  const product = (id: number | null): ProductOption | undefined => products.find((p) => p.id === id);
  const priceOf = (l: LineDraft) => (l.unitPrice === '' ? (product(l.productId)?.salePrice ?? 0) : Number(l.unitPrice));
  const subtotal = (l: LineDraft) => Number(l.quantity || 0) * priceOf(l);
  const total = lines.reduce((sum, l) => sum + subtotal(l), 0);
  const filled = lines.filter((l) => l.productId !== null);

  const setLine = (key: number, patch: Partial<LineDraft>) => setLines(lines.map((l) => (l.key === key ? { ...l, ...patch } : l)));

  const confirm = async () => {
    setProcessing(true);
    let created: Invoice | undefined;
    const ok = await toast.run(async () => {
      created = await salesApi.issueInvoice({
        customerId: customerId!,
        date: invoiceDate,
        items: filled.map((l) => ({ productId: l.productId!, quantity: Number(l.quantity), unitPrice: l.unitPrice === '' ? undefined : Number(l.unitPrice) })),
      });
    });
    setProcessing(false);
    if (ok && created) {
      toast.success(__('Invoice :number posted.').replace(':number', created.number));
      navigate(route('sales.invoices.show', { id: created.id }), { replace: true });
    }
  };

  const canConfirm = customerId !== null && filled.length > 0 && filled.every((l) => Number(l.quantity) > 0) && !processing;

  return (
    <>
      <ControlPanel breadcrumbs={[{ label: __('Invoices'), to: route('sales.invoices') }, { label: __('New') }]} />
      <StatusBar stages={STAGES} current="draft">
        <button className="btn" onClick={() => void confirm()} disabled={!canConfirm}>{__('Confirm')}</button>
        <button className="btn-secondary" onClick={() => navigate(route('sales.invoices'))}>{__('Discard')}</button>
      </StatusBar>
      <Sheet>
        <SheetTitle overline={__('Customer Invoice')}><span className="text-gray-400">{__('Draft')}</span></SheetTitle>
        <Columns>
          <FieldGroup>
            <Field label={__('Customer')} htmlFor="inv-customer">
              <Many2One
                id="inv-customer"
                autoFocus
                placeholder={__('Search a customer…')}
                options={customers.filter((c) => c.customer && c.active).map((c) => ({ value: c.id, label: c.name }))}
                value={customerId}
                onChange={setCustomerId}
              />
            </Field>
          </FieldGroup>
          <FieldGroup>
            <Field label={__('Invoice date')} htmlFor="inv-date">
              <input id="inv-date" type="date" className="o-input" value={invoiceDate} onChange={(e) => setInvoiceDate(e.target.value)} />
            </Field>
          </FieldGroup>
        </Columns>

        <Notebook
          tabs={[{
            key: 'lines',
            label: __('Invoice lines'),
            content: (
              <>
                <table className="w-full">
                  <thead className="border-b border-gray-300">
                    <tr>
                      <th className="th w-1/2">{__('Product')}</th>
                      <th className="th text-right">{__('Quantity')}</th>
                      <th className="th text-right">{__('Unit price')}</th>
                      <th className="th text-right">{__('Subtotal')}</th>
                      <th className="th w-8" />
                    </tr>
                  </thead>
                  <tbody>
                    {lines.map((l) => {
                      const p = product(l.productId);
                      const short = p !== undefined && Number(l.quantity) > p.quantity;
                      return (
                        <tr key={l.key} className="border-b border-gray-100 align-top">
                          <td className="td">
                            <Many2One
                              aria-label={__('Product')}
                              placeholder={__('Select a product…')}
                              options={products.filter((o) => o.active).map((o) => ({ value: o.id, label: `[${o.code}] ${o.name}`, hint: `${quantity(o.quantity)} ${o.unit.toLowerCase()}` }))}
                              value={l.productId}
                              onChange={(v) => setLine(l.key, { productId: v, unitPrice: '' })}
                            />
                            {short && <p className="mt-1 text-xs text-red-600">{__('Only :qty in stock').replace(':qty', `${quantity(p.quantity)} ${p.unit.toLowerCase()}`)}</p>}
                          </td>
                          <td className="td">
                            <input aria-label={__('Quantity')} type="number" min="0.001" step="0.001" className={`o-input text-right ${short ? 'text-red-600' : ''}`} value={l.quantity} onChange={(e) => setLine(l.key, { quantity: e.target.value })} />
                          </td>
                          <td className="td">
                            <input aria-label={__('Unit price')} type="number" min="0" step="0.01" className="o-input text-right" placeholder={p ? money(p.salePrice) : '0.00'} value={l.unitPrice} onChange={(e) => setLine(l.key, { unitPrice: e.target.value })} />
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
                <button type="button" className="btn-link mt-2 px-3" onClick={() => setLines([...lines, emptyLine()])}>{__('Add a line')}</button>
                <Totals rows={[{ label: __('Total'), value: money(total), strong: true }]} />
              </>
            ),
          }]}
        />
      </Sheet>
    </>
  );
}

function ExistingInvoice({ id }: { id: number }) {
  const loadInvoice = useCallback(() => salesApi.invoice(id), [id]);
  const { data: invoice, error, reload } = useLoad<Invoice | null>(loadInvoice, null);
  const { data: payments, reload: reloadPayments } = useLoad(salesApi.payments, []);
  const { data: customers } = useLoad(salesApi.customerOptions, []);
  const { data: creditNotes, reload: reloadCreditNotes } = useLoad(salesApi.creditNotes, []);
  const [dialog, setDialog] = useState<'pay' | 'credit' | null>(null);

  if (error !== null) return <p className="p-6 text-red-600" role="alert">{error}</p>;
  if (invoice === null) return <p className="p-6 text-gray-500">{__('Loading…')}</p>;

  const related = payments.filter((p) => p.invoiceNumber === invoice.number);
  const credits = creditNotes.filter((c) => c.invoiceId === invoice.id);
  const returnable = invoice.lines.some((l) => l.returnable > 0);

  return (
    <>
      <ControlPanel breadcrumbs={[{ label: __('Invoices'), to: route('sales.invoices') }, { label: invoice.number }]} />
      <StatusBar stages={STAGES} current="posted">
        {invoice.outstanding > 0 && (
          <button className="btn" onClick={() => setDialog('pay')}>{__('Register payment')}</button>
        )}
        {returnable && <button className="btn-secondary" onClick={() => setDialog('credit')}><Undo2 className="h-4 w-4" />{__('Credit note')}</button>}
        <button className="btn-secondary" onClick={() => window.print()}><Printer className="h-4 w-4" />{__('Print')}</button>
      </StatusBar>
      <Sheet ribbon={RIBBON[invoice.status] && <Ribbon tone={RIBBON[invoice.status]!.tone}>{RIBBON[invoice.status]!.label}</Ribbon>}>
        <SmartButtons>
          <SmartButton icon={<Banknote className="h-5 w-5" />} value={related.length} label={__('Payments')} />
          <SmartButton icon={<Undo2 className="h-5 w-5" />} value={credits.length} label={__('Credit notes')} />
        </SmartButtons>
        <SheetTitle overline={__('Customer Invoice')}>{invoice.number}</SheetTitle>
        <Columns>
          <FieldGroup>
            <Field label={__('Customer')}><span className="font-medium text-accent">{invoice.customerName}</span></Field>
            <Field label={__('Salesperson')}>{invoice.createdBy}</Field>
          </FieldGroup>
          <FieldGroup>
            <Field label={__('Invoice date')}>{formatDate(invoice.invoiceDate)}</Field>
            <Field label={__('Payment status')}><Badge tone={STATUS[invoice.status].tone}>{STATUS[invoice.status].label}</Badge></Field>
          </FieldGroup>
        </Columns>

        <Notebook
          tabs={[
            {
              key: 'lines',
              label: __('Invoice lines'),
              content: (
                <>
                  <table className="w-full">
                    <thead className="border-b border-gray-300">
                      <tr>
                        <th className="th">{__('Product')}</th>
                        <th className="th text-right">{__('Quantity')}</th>
                        <th className="th text-right">{__('Unit price')}</th>
                        <th className="th text-right">{__('Subtotal')}</th>
                      </tr>
                    </thead>
                    <tbody>
                      {invoice.lines.map((l, i) => (
                        <tr key={i} className="border-b border-gray-100">
                          <td className="td">{l.productName}</td>
                          <td className="td text-right tabular-nums">{quantity(l.quantity)} {l.unit.toLowerCase()}</td>
                          <td className="td text-right tabular-nums">{money(l.unitPrice)}</td>
                          <td className="td text-right tabular-nums">{money(l.lineTotal)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                  <Totals rows={[
                    { label: __('Total'), value: money(invoice.total), strong: true },
                    { label: __('Paid'), value: money(invoice.paid) },
                    ...(invoice.credited > 0 ? [{ label: __('Credited'), value: money(invoice.credited) }] : []),
                    { label: __('Amount due'), value: money(invoice.outstanding), strong: true },
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
            {
              key: 'credit-notes',
              label: __('Credit notes'),
              content: credits.length === 0 ? (
                <p className="text-gray-500">{__('No goods returned on this invoice.')}</p>
              ) : (
                <table className="w-full">
                  <thead className="border-b border-gray-300">
                    <tr><th className="th">{__('Number')}</th><th className="th">{__('Date')}</th><th className="th">{__('Reason')}</th><th className="th">{__('Products')}</th><th className="th text-right">{__('Total')}</th></tr>
                  </thead>
                  <tbody>
                    {credits.map((c) => (
                      <tr key={c.id} className="border-b border-gray-100 align-top">
                        <td className="td font-medium">{c.number}</td><td className="td">{formatDate(c.date)}</td><td className="td">{c.reason}</td>
                        <td className="td text-gray-600">{c.lines.map((l) => `${l.productName} × ${quantity(l.quantity)}`).join(', ')}</td>
                        <td className="td text-right tabular-nums">{money(c.total)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              ),
            },
          ]}
        />
      </Sheet>
      {dialog === 'credit' && (
        <CreditNoteDialog
          invoice={invoice}
          onClose={() => setDialog(null)}
          onDone={() => {
            setDialog(null);
            void Promise.all([reload(), reloadCreditNotes()]);
          }}
        />
      )}
      {dialog === 'pay' && (
        <RegisterPaymentDialog
          invoice={invoice}
          customers={customers}
          invoices={[invoice]}
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
