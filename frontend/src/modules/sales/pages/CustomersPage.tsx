import { Phone } from 'lucide-react';
import { useCallback, useState } from 'react';
import { date, money } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { useLoad } from '../../../lib/useLoad';
import { ControlPanel, type ViewMode } from '../../../ui/ControlPanel';
import { Dialog } from '../../../ui/Dialog';
import { Avatar, KanbanGrid } from '../../../ui/Kanban';
import { ListView, type Column } from '../../../ui/ListView';
import { useSearch } from '../../../ui/search';
import { salesApi } from '../api';
import type { CustomerBalance, LedgerRow } from '../types';

const balanceClass = (balance: number) => (balance > 0 ? 'text-red-600' : balance < 0 ? 'text-emerald-700' : 'text-gray-500');

export function CustomersPage() {
  const { data: customers, loading } = useLoad(salesApi.customerBalances, []);
  const [view, setView] = useState<ViewMode>('kanban');
  const [selected, setSelected] = useState<CustomerBalance | null>(null);

  const search = useSearch(customers, {
    text: (c) => [c.name, c.phone],
    filters: [
      { key: 'owing', group: 'balance', label: __('Owes us'), test: (c) => c.balance > 0 },
      { key: 'credit', group: 'balance', label: __('Has credit'), test: (c) => c.balance < 0 },
      { key: 'settled', group: 'balance', label: __('Settled'), test: (c) => c.balance === 0 },
    ],
  });

  const columns: readonly Column<CustomerBalance>[] = [
    { key: 'name', label: __('Customer'), render: (c) => <span className="font-medium">{c.name}</span>, sortValue: (c) => c.name },
    { key: 'phone', label: __('Phone'), render: (c) => c.phone },
    { key: 'balance', label: __('Balance'), align: 'right', render: (c) => <span className={balanceClass(c.balance)}>{money(c.balance)}</span>, sortValue: (c) => c.balance, sum: (c) => c.balance, formatSum: money },
  ];

  return (
    <>
      <ControlPanel breadcrumbs={[{ label: __('Customers') }]} search={search} view={{ mode: view, onChange: setView }} />
      {view === 'kanban' ? (
        <KanbanGrid
          rows={search.results}
          rowKey={(c) => c.partnerId}
          onOpen={setSelected}
          renderCard={(c) => (
            <div className="flex gap-3">
              <Avatar name={c.name} />
              <div className="min-w-0 flex-1">
                <div className="truncate font-semibold">{c.name}</div>
                {c.phone && <div className="flex items-center gap-1 text-xs text-gray-500"><Phone className="h-3 w-3" />{c.phone}</div>}
                <div className="mt-2 flex items-baseline justify-between text-xs text-gray-500">
                  {__('Balance')}
                  <span className={`text-sm font-semibold tabular-nums ${balanceClass(c.balance)}`}>{money(c.balance)}</span>
                </div>
              </div>
            </div>
          )}
        />
      ) : (
        <ListView rows={search.results} columns={columns} rowKey={(c) => c.partnerId} loading={loading} onOpen={setSelected} />
      )}
      {selected && <CustomerLedgerDialog customer={selected} onClose={() => setSelected(null)} />}
    </>
  );
}

function CustomerLedgerDialog({ customer, onClose }: { customer: CustomerBalance; onClose: () => void }) {
  const load = useCallback(() => salesApi.customerLedger(customer.partnerId), [customer.partnerId]);
  const { data: rows, loading } = useLoad<LedgerRow[]>(load, []);

  const columns: readonly Column<LedgerRow>[] = [
    { key: 'date', label: __('Date'), render: (r) => date(r.date) },
    { key: 'ref', label: __('Reference'), render: (r) => r.reference },
    { key: 'desc', label: __('Label'), render: (r) => r.description },
    { key: 'debit', label: __('Debit'), align: 'right', render: (r) => (r.debit > 0 ? money(r.debit) : ''), sum: (r) => r.debit, formatSum: money },
    { key: 'credit', label: __('Credit'), align: 'right', render: (r) => (r.credit > 0 ? money(r.credit) : ''), sum: (r) => r.credit, formatSum: money },
    { key: 'balance', label: __('Balance'), align: 'right', render: (r) => <span className="font-medium">{money(r.balance)}</span> },
  ];

  return (
    <Dialog title={__('Customer ledger — :name').replace(':name', customer.name)} onClose={onClose} size="lg">
      <div className="-mx-5 max-h-[60vh] overflow-y-auto">
        <ListView rows={rows} columns={columns} rowKey={(r) => `${r.date}-${r.reference}-${r.balance}`} loading={loading} />
      </div>
    </Dialog>
  );
}
