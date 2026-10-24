import { useNavigate } from 'react-router-dom';
import { date, money } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { route } from '../../../lib/routes';
import { useLoad } from '../../../lib/useLoad';
import { ControlPanel } from '../../../ui/ControlPanel';
import { EmptyState } from '../../../ui/EmptyState';
import { ListView, type Column } from '../../../ui/ListView';
import { useSearch } from '../../../ui/search';
import { salesApi } from '../api';
import type { CreditNote } from '../types';

const COLUMNS: readonly Column<CreditNote>[] = [
  { key: 'number', label: __('Number'), render: (c) => <span className="font-medium">{c.number}</span>, sortValue: (c) => c.number },
  { key: 'invoice', label: __('Invoice'), render: (c) => c.invoiceNumber, sortValue: (c) => c.invoiceNumber },
  { key: 'customer', label: __('Customer'), render: (c) => c.customerName, sortValue: (c) => c.customerName },
  { key: 'date', label: __('Date'), render: (c) => date(c.date), sortValue: (c) => c.date },
  { key: 'reason', label: __('Reason'), render: (c) => c.reason, className: 'text-gray-500' },
  { key: 'total', label: __('Total'), align: 'right', render: (c) => money(c.total), sortValue: (c) => c.total, sum: (c) => c.total, formatSum: money },
];

export function CreditNotesPage() {
  const navigate = useNavigate();
  const { data: notes, loading } = useLoad(salesApi.creditNotes, []);
  const search = useSearch(notes, {
    text: (c) => [c.number, c.invoiceNumber, c.customerName, c.reason],
    groupBys: [
      { key: 'customer', label: __('Customer'), value: (c) => c.customerName },
      { key: 'month', label: __('Month'), value: (c) => c.date.slice(0, 7) },
    ],
  });

  return (
    <>
      <ControlPanel breadcrumbs={[{ label: __('Credit notes') }]} search={search} />
      <ListView
        rows={search.results}
        columns={COLUMNS}
        rowKey={(c) => c.id}
        loading={loading}
        groupBy={search.groupBy}
        onOpen={(c) => navigate(route('sales.invoices.show', { id: c.invoiceId }))}
        empty={<EmptyState title={__('No credit notes')} hint={__('Open an invoice and click "Credit note" to record returned goods. They go back into stock and the customer balance is reduced.')} />}
      />
    </>
  );
}
