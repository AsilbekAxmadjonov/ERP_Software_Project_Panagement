import { Plus } from 'lucide-react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { date, money } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { route } from '../../../lib/routes';
import { useLoad } from '../../../lib/useLoad';
import { Badge } from '../../../ui/Badge';
import { ControlPanel } from '../../../ui/ControlPanel';
import { EmptyState } from '../../../ui/EmptyState';
import { ListView, type Column } from '../../../ui/ListView';
import { useSearch } from '../../../ui/search';
import { salesApi } from '../api';
import { RegisterPaymentDialog } from '../components/RegisterPaymentDialog';
import { CHANNEL_LABEL } from '../labels';
import { PaymentChannel, type Payment } from '../types';

export function PaymentsPage() {
  const navigate = useNavigate();
  const { data: payments, loading, reload } = useLoad(salesApi.payments, []);
  const { data: customers } = useLoad(salesApi.customerOptions, []);
  const { data: invoices, reload: reloadInvoices } = useLoad(salesApi.invoices, []);
  const [creating, setCreating] = useState(false);

  const invoiceIdByNumber = new Map(invoices.map((i) => [i.number, i.id]));

  const columns: readonly Column<Payment>[] = [
    { key: 'date', label: __('Date'), render: (p) => date(p.date), sortValue: (p) => p.date },
    { key: 'customer', label: __('Customer'), render: (p) => p.customerName, sortValue: (p) => p.customerName },
    {
      key: 'invoice',
      label: __('Invoice'),
      render: (p) => {
        const invoiceId = p.invoiceNumber === null ? undefined : invoiceIdByNumber.get(p.invoiceNumber);
        if (invoiceId === undefined) return <Badge tone="blue">{__('Advance')}</Badge>;
        return (
          <button className="btn-link" onClick={(e) => { e.stopPropagation(); navigate(route('sales.invoices.show', { id: invoiceId })); }}>
            {p.invoiceNumber}
          </button>
        );
      },
    },
    { key: 'method', label: __('Journal'), render: (p) => CHANNEL_LABEL[p.method] },
    { key: 'note', label: __('Memo'), render: (p) => p.note, className: 'text-gray-500' },
    { key: 'amount', label: __('Amount'), align: 'right', render: (p) => money(p.amount), sortValue: (p) => p.amount, sum: (p) => p.amount, formatSum: money },
  ];

  const search = useSearch(payments, {
    text: (p) => [p.customerName, p.invoiceNumber, p.note],
    filters: [
      { key: 'cash', group: 'method', label: __('Cash'), test: (p) => p.method === PaymentChannel.CASH },
      { key: 'bank', group: 'method', label: __('Bank'), test: (p) => p.method === PaymentChannel.BANK },
      { key: 'advance', label: __('Advance payments'), test: (p) => p.invoiceNumber === null },
    ],
    groupBys: [
      { key: 'customer', label: __('Customer'), value: (p) => p.customerName },
      { key: 'method', label: __('Journal'), value: (p) => CHANNEL_LABEL[p.method] },
      { key: 'month', label: __('Month'), value: (p) => p.date.slice(0, 7) },
    ],
  });

  return (
    <>
      <ControlPanel
        breadcrumbs={[{ label: __('Customer payments') }]}
        actions={<button className="btn" onClick={() => setCreating(true)}><Plus className="h-4 w-4" />{__('New')}</button>}
        search={search}
      />
      <ListView
        rows={search.results}
        columns={columns}
        rowKey={(p) => p.id}
        loading={loading}
        groupBy={search.groupBy}
        empty={<EmptyState title={__('No customer payments yet')} hint={__('Register money received from customers in cash or by bank transfer.')} action={<button className="btn" onClick={() => setCreating(true)}>{__('Register a payment')}</button>} />}
      />
      {creating && (
        <RegisterPaymentDialog
          customers={customers}
          invoices={invoices}
          onClose={() => setCreating(false)}
          onDone={() => {
            setCreating(false);
            void Promise.all([reload(), reloadInvoices()]);
          }}
        />
      )}
    </>
  );
}
