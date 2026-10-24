import { Plus } from 'lucide-react';
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
import { ORDER_STATUS, PAYMENT_STATUS } from '../labels';
import { OrderStatus, PaymentProgress, type PurchaseOrder } from '../types';

const COLUMNS: readonly Column<PurchaseOrder>[] = [
  { key: 'number', label: __('Reference'), render: (o) => <span className="font-medium">{o.number}</span>, sortValue: (o) => o.number },
  { key: 'vendor', label: __('Vendor'), render: (o) => o.supplierName, sortValue: (o) => o.supplierName },
  { key: 'date', label: __('Order date'), render: (o) => date(o.orderDate), sortValue: (o) => o.orderDate },
  { key: 'received', label: __('Received on'), render: (o) => (o.receivedDate ? date(o.receivedDate) : ''), sortValue: (o) => o.receivedDate ?? '' },
  { key: 'buyer', label: __('Buyer'), render: (o) => o.createdBy, className: 'text-gray-500' },
  { key: 'total', label: __('Total'), align: 'right', render: (o) => money(o.total), sortValue: (o) => o.total, sum: (o) => o.total, formatSum: money },
  { key: 'due', label: __('Amount due'), align: 'right', render: (o) => money(o.outstanding), sortValue: (o) => o.outstanding, sum: (o) => o.outstanding, formatSum: money },
  { key: 'status', label: __('Status'), render: (o) => <Badge tone={ORDER_STATUS[o.status].tone}>{ORDER_STATUS[o.status].label}</Badge> },
  {
    key: 'payment',
    label: __('Payment'),
    render: (o) => (o.status === OrderStatus.RECEIVED ? <Badge tone={PAYMENT_STATUS[o.paymentStatus].tone}>{PAYMENT_STATUS[o.paymentStatus].label}</Badge> : null),
  },
];

export function PurchaseOrdersPage() {
  const navigate = useNavigate();
  const { data: orders, loading } = useLoad(purchaseApi.orders, []);
  const search = useSearch(orders, {
    text: (o) => [o.number, o.supplierName],
    filters: [
      { key: 'rfq', group: 'status', label: __('RFQs'), test: (o) => o.status === OrderStatus.DRAFT },
      { key: 'confirmed', group: 'status', label: __('Waiting for receipt'), test: (o) => o.status === OrderStatus.CONFIRMED },
      { key: 'received', group: 'status', label: __('Received'), test: (o) => o.status === OrderStatus.RECEIVED },
      { key: 'cancelled', group: 'status', label: __('Cancelled'), test: (o) => o.status === OrderStatus.CANCELLED },
      { key: 'to-pay', label: __('To pay'), test: (o) => o.status === OrderStatus.RECEIVED && o.paymentStatus !== PaymentProgress.PAID },
    ],
    groupBys: [
      { key: 'vendor', label: __('Vendor'), value: (o) => o.supplierName },
      { key: 'status', label: __('Status'), value: (o) => ORDER_STATUS[o.status].label },
      { key: 'month', label: __('Order month'), value: (o) => o.orderDate.slice(0, 7) },
    ],
  });

  const create = () => navigate(route('purchase.orders.new'));

  return (
    <>
      <ControlPanel
        breadcrumbs={[{ label: __('Purchase orders') }]}
        actions={<button className="btn" onClick={create}><Plus className="h-4 w-4" />{__('New')}</button>}
        search={search}
      />
      <ListView
        rows={search.results}
        columns={COLUMNS}
        rowKey={(o) => o.id}
        loading={loading}
        groupBy={search.groupBy}
        rowClassName={(o) => (o.status === OrderStatus.CANCELLED ? 'text-gray-400' : '')}
        onOpen={(o) => navigate(route('purchase.orders.show', { id: o.id }))}
        empty={<EmptyState title={__('Create your first request for quotation')} hint={__('Confirm it into a purchase order, then receive the goods to add them to stock and record what you owe the vendor.')} action={<button className="btn" onClick={create}>{__('New RFQ')}</button>} />}
      />
    </>
  );
}
