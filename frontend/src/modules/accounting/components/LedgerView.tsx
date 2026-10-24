import { useNavigate } from 'react-router-dom';
import { date, money } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { route } from '../../../lib/routes';
import { EmptyState } from '../../../ui/EmptyState';
import { ListView, type Column } from '../../../ui/ListView';
import type { LedgerRow } from '../types';

type Row = LedgerRow & { index: number };

const COLUMNS: readonly Column<Row>[] = [
  { key: 'date', label: __('Date'), render: (r) => date(r.date) },
  { key: 'reference', label: __('Reference'), render: (r) => <span className="font-medium">{r.reference}</span> },
  { key: 'description', label: __('Label'), render: (r) => r.description, className: 'text-gray-600' },
  { key: 'debit', label: __('Debit'), align: 'right', render: (r) => (r.debit > 0 ? money(r.debit) : ''), sum: (r) => r.debit, formatSum: money },
  { key: 'credit', label: __('Credit'), align: 'right', render: (r) => (r.credit > 0 ? money(r.credit) : ''), sum: (r) => r.credit, formatSum: money },
  { key: 'balance', label: __('Balance'), align: 'right', render: (r) => <span className="font-medium">{money(r.balance)}</span> },
];

/** Ledger lines with a running balance, in posting order; a row opens its journal entry. */
export function LedgerView({ rows, loading, selected }: { rows: readonly LedgerRow[]; loading: boolean; selected: boolean }) {
  const navigate = useNavigate();
  if (!selected) return <EmptyState title={__('Pick what to look at')} hint={__('Choose an account or partner above to see its ledger with a running balance.')} />;
  return (
    <ListView
      rows={rows.map((r, index) => ({ ...r, index }))}
      columns={COLUMNS}
      rowKey={(r) => r.index}
      loading={loading}
      onOpen={(r) => navigate(route('accounting.journal.show', { id: r.entryId }))}
    />
  );
}
