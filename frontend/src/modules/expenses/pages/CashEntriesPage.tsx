import { Plus } from 'lucide-react';
import { useCallback, useState } from 'react';
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
import { expensesApi } from '../api';
import { CashEntryDialog } from '../components/CashEntryDialog';
import { CHANNEL_LABEL, STATE, TYPE } from '../labels';
import { EntryState, EntryType, PaymentChannel, type CashEntry } from '../types';

const COLUMNS: readonly Column<CashEntry>[] = [
  { key: 'number', label: __('Reference'), render: (e) => <span className="font-medium">{e.number}</span>, sortValue: (e) => e.number },
  { key: 'date', label: __('Date'), render: (e) => date(e.date), sortValue: (e) => e.date },
  { key: 'category', label: __('Category'), render: (e) => e.categoryName, sortValue: (e) => e.categoryName },
  { key: 'description', label: __('Description'), render: (e) => e.description, className: 'text-gray-500' },
  { key: 'method', label: __('Journal'), render: (e) => CHANNEL_LABEL[e.method] },
  { key: 'user', label: __('Recorded by'), render: (e) => e.createdBy, className: 'text-gray-500' },
  {
    key: 'amount',
    label: __('Amount'),
    align: 'right',
    render: (e) => money(e.amount),
    sortValue: (e) => e.amount,
    sum: (e) => (e.status === EntryState.POSTED ? e.amount : 0),
    formatSum: money,
  },
  { key: 'status', label: __('Status'), render: (e) => <Badge tone={STATE[e.status].tone}>{STATE[e.status].label}</Badge> },
];

export function CashEntriesPage({ type }: { type: EntryType }) {
  const navigate = useNavigate();
  const loadEntries = useCallback(() => expensesApi.entries(type), [type]);
  const { data: entries, loading, reload } = useLoad(loadEntries, []);
  const { data: categories } = useLoad(expensesApi.categories, []);
  const [creating, setCreating] = useState(false);

  const search = useSearch(entries, {
    text: (e) => [e.number, e.categoryName, e.description],
    defaultFilters: ['posted'],
    filters: [
      { key: 'posted', group: 'status', label: __('Posted'), test: (e) => e.status === EntryState.POSTED },
      { key: 'cancelled', group: 'status', label: __('Cancelled'), test: (e) => e.status === EntryState.CANCELLED },
      { key: 'cash', group: 'method', label: __('Cash'), test: (e) => e.method === PaymentChannel.CASH },
      { key: 'bank', group: 'method', label: __('Bank'), test: (e) => e.method === PaymentChannel.BANK },
    ],
    groupBys: [
      { key: 'category', label: __('Category'), value: (e) => e.categoryName },
      { key: 'method', label: __('Journal'), value: (e) => CHANNEL_LABEL[e.method] },
      { key: 'month', label: __('Month'), value: (e) => e.date.slice(0, 7) },
    ],
  });

  const create = () => setCreating(true);

  return (
    <>
      <ControlPanel
        breadcrumbs={[{ label: TYPE[type].plural }]}
        actions={<button className="btn" onClick={create}><Plus className="h-4 w-4" />{__('New')}</button>}
        search={search}
      />
      <ListView
        rows={search.results}
        columns={COLUMNS}
        rowKey={(e) => e.id}
        loading={loading}
        groupBy={search.groupBy}
        rowClassName={(e) => (e.status === EntryState.CANCELLED ? 'text-gray-400 line-through' : '')}
        onOpen={(e) => navigate(route('expenses.entries.show', { id: e.id }))}
        empty={<EmptyState title={TYPE[type].empty} hint={TYPE[type].hint} action={<button className="btn" onClick={create}>{TYPE[type].newTitle}</button>} />}
      />
      {creating && (
        <CashEntryDialog
          type={type}
          categories={categories}
          onClose={() => setCreating(false)}
          onDone={() => {
            setCreating(false);
            void reload();
          }}
        />
      )}
    </>
  );
}

export function ExpensesPage() {
  return <CashEntriesPage key={EntryType.EXPENSE} type={EntryType.EXPENSE} />;
}

export function OtherIncomePage() {
  return <CashEntriesPage key={EntryType.INCOME} type={EntryType.INCOME} />;
}
