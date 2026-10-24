import { Mail, Phone, Plus } from 'lucide-react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { __ } from '../../../lib/i18n';
import { route } from '../../../lib/routes';
import { useLoad } from '../../../lib/useLoad';
import { Badge } from '../../../ui/Badge';
import { ControlPanel, type ViewMode } from '../../../ui/ControlPanel';
import { EmptyState } from '../../../ui/EmptyState';
import { Avatar, KanbanGrid } from '../../../ui/Kanban';
import { ListView, type Column } from '../../../ui/ListView';
import { useSearch } from '../../../ui/search';
import { partnersApi } from '../api';
import { KIND } from '../labels';
import type { Partner } from '../types';

const COLUMNS: readonly Column<Partner>[] = [
  {
    key: 'name',
    label: __('Name'),
    render: (p) => <span className="inline-flex items-center gap-2 font-medium"><Avatar name={p.name} size="sm" />{p.name}</span>,
    sortValue: (p) => p.name,
  },
  { key: 'type', label: __('Type'), render: (p) => <Badge tone={KIND[p.type].tone}>{KIND[p.type].label}</Badge>, sortValue: (p) => p.type },
  { key: 'contact', label: __('Contact person'), render: (p) => p.contactPerson },
  { key: 'phone', label: __('Phone'), render: (p) => p.phone },
  { key: 'email', label: __('Email'), render: (p) => p.email },
  { key: 'tax', label: __('Tax ID'), render: (p) => p.taxId, className: 'text-gray-500' },
];

export function ContactsPage() {
  const navigate = useNavigate();
  const [mode, setMode] = useState<ViewMode>('kanban');
  const { data: partners, loading } = useLoad(partnersApi.list, []);
  const search = useSearch(partners, {
    text: (p) => [p.name, p.contactPerson, p.phone, p.email, p.taxId],
    defaultFilters: ['active'],
    filters: [
      { key: 'customers', group: 'role', label: __('Customers'), test: (p) => p.customer },
      { key: 'suppliers', group: 'role', label: __('Vendors'), test: (p) => p.supplier },
      { key: 'active', group: 'state', label: __('Active'), test: (p) => p.active },
      { key: 'archived', group: 'state', label: __('Archived'), test: (p) => !p.active },
    ],
    groupBys: [{ key: 'type', label: __('Type'), value: (p) => KIND[p.type].label }],
  });

  const create = () => navigate(route('partners.new'));
  const open = (p: Partner) => navigate(route('partners.show', { id: p.id }));

  return (
    <>
      <ControlPanel
        breadcrumbs={[{ label: __('Contacts') }]}
        actions={<button className="btn" onClick={create}><Plus className="h-4 w-4" />{__('New')}</button>}
        search={search}
        view={{ mode, onChange: setMode }}
      />
      {!loading && partners.length === 0 ? (
        <EmptyState title={__('Create a contact in your address book')} hint={__('Customers and vendors you sell to and buy from.')} action={<button className="btn" onClick={create}>{__('New contact')}</button>} />
      ) : mode === 'kanban' && search.groupBy === null ? (
        <KanbanGrid
          rows={search.results}
          rowKey={(p) => p.id}
          onOpen={open}
          renderCard={(p) => (
            <div className="flex gap-3">
              <Avatar name={p.name} size="lg" />
              <div className="min-w-0 flex-1 space-y-0.5">
                <div className="truncate font-semibold">{p.name}</div>
                {p.contactPerson && <div className="truncate text-xs text-gray-500">{p.contactPerson}</div>}
                {p.phone && <div className="flex items-center gap-1 truncate text-xs text-gray-600"><Phone className="h-3 w-3" />{p.phone}</div>}
                {p.email && <div className="flex items-center gap-1 truncate text-xs text-gray-600"><Mail className="h-3 w-3" />{p.email}</div>}
                <div className="flex gap-1 pt-1">
                  <Badge tone={KIND[p.type].tone}>{KIND[p.type].label}</Badge>
                  {!p.active && <Badge tone="gray">{__('Archived')}</Badge>}
                </div>
              </div>
            </div>
          )}
        />
      ) : (
        <ListView
          rows={search.results}
          columns={COLUMNS}
          rowKey={(p) => p.id}
          loading={loading}
          groupBy={search.groupBy}
          rowClassName={(p) => (p.active ? '' : 'text-gray-400')}
          onOpen={open}
        />
      )}
    </>
  );
}
