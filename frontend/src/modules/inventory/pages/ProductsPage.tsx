import { Package, Plus, TriangleAlert } from 'lucide-react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { money, quantity } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { route } from '../../../lib/routes';
import { useLoad } from '../../../lib/useLoad';
import { Badge } from '../../../ui/Badge';
import { ControlPanel, type ViewMode } from '../../../ui/ControlPanel';
import { EmptyState } from '../../../ui/EmptyState';
import { KanbanGrid } from '../../../ui/Kanban';
import { ListView, type Column } from '../../../ui/ListView';
import { useSearch } from '../../../ui/search';
import { inventoryApi } from '../api';
import { UNIT_LABEL } from '../labels';
import type { Product } from '../types';

const onHandClass = (p: Product) => (p.quantity <= 0 ? 'text-red-600' : p.lowStock ? 'text-amber-600' : '');

const COLUMNS: readonly Column<Product>[] = [
  { key: 'code', label: __('Reference'), render: (p) => <span className="font-mono">{p.code}</span>, sortValue: (p) => p.code },
  { key: 'name', label: __('Product'), render: (p) => <span className="font-medium">{p.name}</span>, sortValue: (p) => p.name },
  { key: 'unit', label: __('Unit'), render: (p) => UNIT_LABEL[p.unit] },
  { key: 'price', label: __('Sales price'), align: 'right', render: (p) => money(p.salePrice), sortValue: (p) => p.salePrice },
  { key: 'cost', label: __('Avg cost'), align: 'right', render: (p) => money(p.averageCost), sortValue: (p) => p.averageCost },
  { key: 'qty', label: __('On hand'), align: 'right', render: (p) => <span className={onHandClass(p)}>{quantity(p.quantity)}</span>, sortValue: (p) => p.quantity },
  { key: 'min', label: __('Min. qty'), align: 'right', render: (p) => (p.reorderLevel === null ? '' : quantity(p.reorderLevel)), className: 'text-gray-500' },
  { key: 'value', label: __('Stock value'), align: 'right', render: (p) => money(p.stockValue), sortValue: (p) => p.stockValue, sum: (p) => p.stockValue, formatSum: money },
  { key: 'alert', label: '', render: (p) => (p.lowStock ? <Badge tone="yellow">{__('Low stock')}</Badge> : null) },
];

export function ProductsPage() {
  const navigate = useNavigate();
  const [mode, setMode] = useState<ViewMode>('kanban');
  const { data: products, loading } = useLoad(inventoryApi.products, []);
  const search = useSearch(products, {
    text: (p) => [p.code, p.name],
    defaultFilters: ['active'],
    filters: [
      { key: 'low', group: 'stock', label: __('Low stock'), test: (p) => p.lowStock },
      { key: 'out', group: 'stock', label: __('Out of stock'), test: (p) => p.quantity <= 0 },
      { key: 'active', group: 'state', label: __('Active'), test: (p) => p.active },
      { key: 'archived', group: 'state', label: __('Archived'), test: (p) => !p.active },
    ],
    groupBys: [{ key: 'unit', label: __('Unit of measure'), value: (p) => UNIT_LABEL[p.unit] }],
  });

  const create = () => navigate(route('inventory.products.new'));
  const open = (p: Product) => navigate(route('inventory.products.show', { id: p.id }));
  const lowCount = products.filter((p) => p.active && p.lowStock).length;

  return (
    <>
      <ControlPanel
        breadcrumbs={[{ label: __('Products') }]}
        actions={<button className="btn" onClick={create}><Plus className="h-4 w-4" />{__('New')}</button>}
        search={search}
        view={{ mode, onChange: setMode }}
      />
      {lowCount > 0 && !search.active.includes('low') && (
        <div className="flex items-center gap-2 border-b border-amber-200 bg-amber-50 px-4 py-2 text-sm text-amber-800" role="status">
          <TriangleAlert className="h-4 w-4" />
          {(lowCount === 1 ? __('1 product is below its minimum quantity.') : __(':count products are below their minimum quantity.')).replace(':count', String(lowCount))}
          <button className="btn-link" onClick={() => search.toggle('low')}>{__('Show them')}</button>
        </div>
      )}
      {!loading && products.length === 0 ? (
        <EmptyState title={__('Create your first product')} hint={__('Products are what you buy, stock and sell.')} action={<button className="btn" onClick={create}>{__('New product')}</button>} />
      ) : mode === 'kanban' && search.groupBy === null ? (
        <KanbanGrid
          rows={search.results}
          rowKey={(p) => p.id}
          onOpen={open}
          renderCard={(p) => (
            <div className="flex gap-3">
              <Package className="h-14 w-14 shrink-0 rounded bg-gray-100 p-3 text-gray-400" aria-hidden="true" />
              <div className="min-w-0 flex-1">
                <div className="truncate font-semibold">{p.name}</div>
                <div className="text-xs text-gray-500">[{p.code}]</div>
                <div className="mt-1 text-sm">{__('Price')}: {money(p.salePrice)}</div>
                <div className={`text-sm ${onHandClass(p)}`}>{__('On hand')}: {quantity(p.quantity)} {UNIT_LABEL[p.unit]}</div>
              </div>
              <div className="flex flex-col items-end gap-1">
                {p.lowStock && <Badge tone="yellow">{__('Low')}</Badge>}
                {!p.active && <Badge tone="gray">{__('Archived')}</Badge>}
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
