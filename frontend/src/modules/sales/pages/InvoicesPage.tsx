import { Plus } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { date, money, today } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { route } from '../../../lib/routes';
import { useLoad } from '../../../lib/useLoad';
import { Badge } from '../../../ui/Badge';
import { ControlPanel } from '../../../ui/ControlPanel';
import { EmptyState } from '../../../ui/EmptyState';
import { listSheet } from '../../../ui/export';
import { ListView, type Column } from '../../../ui/ListView';
import { useSearch } from '../../../ui/search';
import { salesApi } from '../api';
import { STATUS } from '../labels';
import { PaymentProgress, type Invoice } from '../types';

const COLUMNS: readonly Column<Invoice>[] = [
  { key: 'number', label: __('Number'), render: (i) => <span className="font-medium">{i.number}</span>, sortValue: (i) => i.number },
  { key: 'customer', label: __('Customer'), render: (i) => i.customerName, sortValue: (i) => i.customerName },
  { key: 'date', label: __('Invoice date'), render: (i) => date(i.invoiceDate), sortValue: (i) => i.invoiceDate },
  { key: 'by', label: __('Salesperson'), render: (i) => i.createdBy, className: 'text-gray-500' },
  { key: 'total', label: __('Total'), align: 'right', render: (i) => money(i.total), sortValue: (i) => i.total, sum: (i) => i.total, formatSum: money },
  { key: 'due', label: __('Amount due'), align: 'right', render: (i) => money(i.outstanding), sortValue: (i) => i.outstanding, sum: (i) => i.outstanding, formatSum: money },
  { key: 'status', label: __('Payment'), render: (i) => <Badge tone={STATUS[i.status].tone}>{STATUS[i.status].label}</Badge>, exportValue: (i) => STATUS[i.status].label },
];

export function InvoicesPage() {
  const navigate = useNavigate();
  const { data: invoices, loading } = useLoad(salesApi.invoices, []);
  const search = useSearch(invoices, {
    text: (i) => [i.number, i.customerName],
    filters: [
      { key: 'unpaid', group: 'status', label: __('Not paid'), test: (i) => i.status === PaymentProgress.UNPAID },
      { key: 'partial', group: 'status', label: __('Partially paid'), test: (i) => i.status === PaymentProgress.PARTIAL },
      { key: 'paid', group: 'status', label: __('Paid'), test: (i) => i.status === PaymentProgress.PAID },
      { key: 'credited', group: 'status', label: __('Credited'), test: (i) => i.status === PaymentProgress.CREDITED },
      { key: 'month', group: 'date', label: __('This month'), test: (i) => i.invoiceDate.slice(0, 7) === today().slice(0, 7) },
    ],
    groupBys: [
      { key: 'customer', label: __('Customer'), value: (i) => i.customerName },
      { key: 'status', label: __('Payment status'), value: (i) => STATUS[i.status].label },
      { key: 'month', label: __('Invoice month'), value: (i) => i.invoiceDate.slice(0, 7) },
    ],
  });

  const create = () => navigate(route('sales.invoices.new'));

  return (
    <>
      <ControlPanel
        breadcrumbs={[{ label: __('Invoices') }]}
        exportData={() => ({ fileName: `invoices_${today()}`, sheets: [listSheet(__('Invoices'), COLUMNS, search.results)] })}
        actions={<button className="btn" onClick={create}><Plus className="h-4 w-4" />{__('New')}</button>}
        search={search}
      />
      <ListView
        rows={search.results}
        columns={COLUMNS}
        rowKey={(i) => i.id}
        loading={loading}
        groupBy={search.groupBy}
        onOpen={(i) => navigate(route('sales.invoices.show', { id: i.id }))}
        empty={<EmptyState title={__('Create your first invoice')} hint={__('Invoices take goods out of stock and post revenue to the ledger automatically.')} action={<button className="btn" onClick={create}>{__('New invoice')}</button>} />}
      />
    </>
  );
}
