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
import { accountingApi } from '../api';
import { ORIGIN } from '../labels';
import { EntryKind, type JournalEntry } from '../types';

const COLUMNS: readonly Column<JournalEntry>[] = [
  { key: 'date', label: __('Date'), render: (e) => date(e.entryDate), sortValue: (e) => e.entryDate },
  { key: 'number', label: __('Number'), render: (e) => <span className="font-medium">#{e.id}</span>, sortValue: (e) => e.id },
  { key: 'reference', label: __('Reference'), render: (e) => e.reference, sortValue: (e) => e.reference ?? '' },
  { key: 'description', label: __('Description'), render: (e) => e.description, className: 'text-gray-600' },
  { key: 'origin', label: __('Source'), render: (e) => <Badge tone={ORIGIN[e.origin].tone}>{ORIGIN[e.origin].label}</Badge>, exportValue: (e) => ORIGIN[e.origin].label },
  { key: 'by', label: __('Created by'), render: (e) => e.createdBy, className: 'text-gray-500' },
  { key: 'total', label: __('Total'), align: 'right', render: (e) => money(e.total), sortValue: (e) => e.total, sum: (e) => e.total, formatSum: money },
  { key: 'state', label: __('Status'), render: (e) => (e.reversed ? <Badge tone="red">{__('Reversed')}</Badge> : <Badge tone="green">{__('Posted')}</Badge>), exportValue: (e) => (e.reversed ? __('Reversed') : __('Posted')) },
];

export function JournalListPage() {
  const navigate = useNavigate();
  const { data: entries, loading } = useLoad(accountingApi.entries, []);
  const search = useSearch(entries, {
    text: (e) => [e.reference, e.description, String(e.id), e.createdBy],
    filters: [
      ...Object.values(EntryKind).map((k) => ({ key: k, group: 'origin', label: ORIGIN[k].label, test: (e: JournalEntry) => e.origin === k })),
      { key: 'reversed', group: 'state', label: __('Reversed'), test: (e) => e.reversed },
      { key: 'active', group: 'state', label: __('Not reversed'), test: (e) => !e.reversed },
    ],
    groupBys: [
      { key: 'origin', label: __('Source'), value: (e) => ORIGIN[e.origin].label },
      { key: 'month', label: __('Month'), value: (e) => e.entryDate.slice(0, 7) },
    ],
  });

  const create = () => navigate(route('accounting.journal.new'));

  return (
    <>
      <ControlPanel
        breadcrumbs={[{ label: __('Journal entries') }]}
        exportData={() => ({ fileName: `journal-entries_${today()}`, sheets: [listSheet(__('Journal entries'), COLUMNS, search.results)] })}
        actions={<button className="btn" onClick={create}><Plus className="h-4 w-4" />{__('New')}</button>}
        search={search}
      />
      <ListView
        rows={search.results}
        columns={COLUMNS}
        rowKey={(e) => e.id}
        loading={loading}
        groupBy={search.groupBy}
        rowClassName={(e) => (e.reversed ? 'text-gray-400' : '')}
        onOpen={(e) => navigate(route('accounting.journal.show', { id: e.id }))}
        empty={<EmptyState title={__('No journal entries yet')} hint={__('Sales, payments and stock adjustments post here automatically. You can also record a manual entry.')} action={<button className="btn" onClick={create}>{__('New entry')}</button>} />}
      />
    </>
  );
}
