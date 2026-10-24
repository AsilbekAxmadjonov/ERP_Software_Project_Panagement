import { Plus } from 'lucide-react';
import { useState } from 'react';
import { __ } from '../../../lib/i18n';
import { useLoad } from '../../../lib/useLoad';
import { Badge } from '../../../ui/Badge';
import { ControlPanel } from '../../../ui/ControlPanel';
import { ListView, type Column } from '../../../ui/ListView';
import { useSearch } from '../../../ui/search';
import { expensesApi } from '../api';
import { CategoryDialog } from '../components/CategoryDialog';
import { TYPE } from '../labels';
import { EntryType, type Category } from '../types';

const COLUMNS: readonly Column<Category>[] = [
  { key: 'name', label: __('Name'), render: (c) => <span className="font-medium">{c.name}</span>, sortValue: (c) => c.name },
  { key: 'type', label: __('Type'), render: (c) => <Badge tone={TYPE[c.type].tone}>{TYPE[c.type].label}</Badge>, sortValue: (c) => c.type },
  { key: 'account', label: __('Ledger account'), render: (c) => c.accountCode, sortValue: (c) => c.accountCode },
  { key: 'active', label: __('Status'), render: (c) => (c.active ? null : <Badge tone="gray">{__('Archived')}</Badge>) },
];

export function CategoriesPage() {
  const { data: categories, loading, reload } = useLoad(expensesApi.categories, []);
  const [editing, setEditing] = useState<Category | 'new' | null>(null);

  const search = useSearch(categories, {
    text: (c) => [c.name, c.accountCode],
    defaultFilters: ['active'],
    filters: [
      { key: 'expense', group: 'type', label: __('Expense categories'), test: (c) => c.type === EntryType.EXPENSE },
      { key: 'income', group: 'type', label: __('Income categories'), test: (c) => c.type === EntryType.INCOME },
      { key: 'active', group: 'state', label: __('Active'), test: (c) => c.active },
      { key: 'archived', group: 'state', label: __('Archived'), test: (c) => !c.active },
    ],
    groupBys: [
      { key: 'type', label: __('Type'), value: (c) => TYPE[c.type].label },
      { key: 'account', label: __('Ledger account'), value: (c) => c.accountCode },
    ],
  });

  return (
    <>
      <ControlPanel
        breadcrumbs={[{ label: __('Categories') }]}
        actions={<button className="btn" onClick={() => setEditing('new')}><Plus className="h-4 w-4" />{__('New')}</button>}
        search={search}
      />
      <ListView
        rows={search.results}
        columns={COLUMNS}
        rowKey={(c) => c.id}
        loading={loading}
        groupBy={search.groupBy}
        rowClassName={(c) => (c.active ? '' : 'text-gray-400')}
        onOpen={setEditing}
      />
      {editing !== null && (
        <CategoryDialog
          category={editing === 'new' ? undefined : editing}
          onClose={() => setEditing(null)}
          onDone={() => {
            setEditing(null);
            void reload();
          }}
        />
      )}
    </>
  );
}
