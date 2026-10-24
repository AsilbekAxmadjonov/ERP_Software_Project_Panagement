import { useNavigate } from 'react-router-dom';
import { date, money, quantity } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { route } from '../../../lib/routes';
import { useLoad } from '../../../lib/useLoad';
import { Badge } from '../../../ui/Badge';
import { ControlPanel } from '../../../ui/ControlPanel';
import { ListView, type Column } from '../../../ui/ListView';
import { useSearch } from '../../../ui/search';
import { inventoryApi } from '../api';
import { MOVEMENT } from '../labels';
import { MovementKind, type StockMovement } from '../types';

const loadAll = () => inventoryApi.movements();

const COLUMNS: readonly Column<StockMovement>[] = [
  { key: 'date', label: __('Date'), render: (m) => date(m.date), sortValue: (m) => m.date },
  { key: 'reference', label: __('Reference'), render: (m) => <span className="font-medium">{m.reference}</span>, sortValue: (m) => m.reference ?? '' },
  { key: 'product', label: __('Product'), render: (m) => m.productName, sortValue: (m) => m.productName },
  { key: 'type', label: __('Type'), render: (m) => <Badge tone={MOVEMENT[m.type].tone}>{MOVEMENT[m.type].label}</Badge> },
  { key: 'reason', label: __('Reason'), render: (m) => m.reason, className: 'text-gray-600' },
  {
    key: 'qty',
    label: __('Quantity'),
    align: 'right',
    render: (m) => <span className={m.quantity < 0 ? 'text-red-600' : 'text-emerald-700'}>{m.quantity > 0 ? '+' : ''}{quantity(m.quantity)}</span>,
    sortValue: (m) => m.quantity,
    sum: (m) => m.quantity,
    formatSum: quantity,
  },
  { key: 'cost', label: __('Unit cost'), align: 'right', render: (m) => money(m.unitCost) },
  { key: 'value', label: __('Value'), align: 'right', render: (m) => money(m.quantity * m.unitCost), sortValue: (m) => m.quantity * m.unitCost, sum: (m) => m.quantity * m.unitCost, formatSum: money },
];

export function StockMovesPage() {
  const navigate = useNavigate();
  const { data: moves, loading } = useLoad(loadAll, []);
  const search = useSearch(moves, {
    text: (m) => [m.productName, m.reference, m.reason],
    filters: [
      { key: 'in', group: 'direction', label: __('Incoming'), test: (m) => m.quantity > 0 },
      { key: 'out', group: 'direction', label: __('Outgoing'), test: (m) => m.quantity < 0 },
      ...Object.values(MovementKind).map((k) => ({ key: k, group: 'type', label: MOVEMENT[k].label, test: (m: StockMovement) => m.type === k })),
    ],
    groupBys: [
      { key: 'product', label: __('Product'), value: (m) => m.productName },
      { key: 'type', label: __('Type'), value: (m) => MOVEMENT[m.type].label },
      { key: 'month', label: __('Month'), value: (m) => m.date.slice(0, 7) },
    ],
  });

  return (
    <>
      <ControlPanel breadcrumbs={[{ label: __('Moves history') }]} search={search} />
      <ListView
        rows={search.results}
        columns={COLUMNS}
        rowKey={(m) => m.id}
        loading={loading}
        groupBy={search.groupBy}
        onOpen={(m) => navigate(route('inventory.products.show', { id: m.productId }))}
      />
    </>
  );
}
