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
import { purchaseApi } from '../api';
import { SupplierPaymentDialog } from '../components/SupplierPaymentDialog';
import { CHANNEL_LABEL } from '../labels';
import { PaymentChannel, type SupplierPayment } from '../types';

export function SupplierPaymentsPage() {
  const navigate = useNavigate();
  const { data: payments, loading, reload } = useLoad(purchaseApi.payments, []);
  const { data: suppliers } = useLoad(purchaseApi.supplierOptions, []);
  const { data: orders, reload: reloadOrders } = useLoad(purchaseApi.orders, []);
  const [creating, setCreating] = useState(false);

  const orderIdByNumber = new Map(orders.map((o) => [o.number, o.id]));

  const columns: readonly Column<SupplierPayment>[] = [
    { key: 'date', label: __('Date'), render: (p) => date(p.date), sortValue: (p) => p.date },
    { key: 'vendor', label: __('Vendor'), render: (p) => p.supplierName, sortValue: (p) => p.supplierName },
    {
      key: 'order',
      label: __('Purchase order'),
      render: (p) => {
        const orderId = p.orderNumber === null ? undefined : orderIdByNumber.get(p.orderNumber);
        if (orderId === undefined) return <Badge tone="blue">{__('Advance')}</Badge>;
        return (
          <button className="btn-link" onClick={(e) => { e.stopPropagation(); navigate(route('purchase.orders.show', { id: orderId })); }}>
            {p.orderNumber}
          </button>
        );
      },
    },
    { key: 'method', label: __('Journal'), render: (p) => CHANNEL_LABEL[p.method] },
    { key: 'note', label: __('Memo'), render: (p) => p.note, className: 'text-gray-500' },
    { key: 'amount', label: __('Amount'), align: 'right', render: (p) => money(p.amount), sortValue: (p) => p.amount, sum: (p) => p.amount, formatSum: money },
  ];

  const search = useSearch(payments, {
    text: (p) => [p.supplierName, p.orderNumber, p.note],
    filters: [
      { key: 'cash', group: 'method', label: __('Cash'), test: (p) => p.method === PaymentChannel.CASH },
      { key: 'bank', group: 'method', label: __('Bank'), test: (p) => p.method === PaymentChannel.BANK },
      { key: 'advance', label: __('Advance payments'), test: (p) => p.orderNumber === null },
    ],
    groupBys: [
      { key: 'vendor', label: __('Vendor'), value: (p) => p.supplierName },
      { key: 'method', label: __('Journal'), value: (p) => CHANNEL_LABEL[p.method] },
      { key: 'month', label: __('Month'), value: (p) => p.date.slice(0, 7) },
    ],
  });

  return (
    <>
      <ControlPanel
        breadcrumbs={[{ label: __('Vendor payments') }]}
        actions={<button className="btn" onClick={() => setCreating(true)}><Plus className="h-4 w-4" />{__('New')}</button>}
        search={search}
      />
      <ListView
        rows={search.results}
        columns={columns}
        rowKey={(p) => p.id}
        loading={loading}
        groupBy={search.groupBy}
        empty={<EmptyState title={__('No vendor payments yet')} hint={__('Register money received from suppliers in cash or by bank transfer.')} action={<button className="btn" onClick={() => setCreating(true)}>{__('Pay a vendor')}</button>} />}
      />
      {creating && (
        <SupplierPaymentDialog
          suppliers={suppliers}
          orders={orders}
          onClose={() => setCreating(false)}
          onDone={() => {
            setCreating(false);
            void Promise.all([reload(), reloadOrders()]);
          }}
        />
      )}
    </>
  );
}
