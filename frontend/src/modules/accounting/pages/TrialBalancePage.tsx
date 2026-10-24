import { money, today } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { useLoad } from '../../../lib/useLoad';
import { Badge } from '../../../ui/Badge';
import { ControlPanel } from '../../../ui/ControlPanel';
import { listSheet } from '../../../ui/export';
import { ListView, type Column } from '../../../ui/ListView';
import { useSearch } from '../../../ui/search';
import { accountingApi } from '../api';
import { KIND_LABEL } from '../labels';
import type { TrialBalance } from '../types';

type Row = TrialBalance['rows'][number];

const EMPTY: TrialBalance = { rows: [], totalDebit: 0, totalCredit: 0 };

const COLUMNS: readonly Column<Row>[] = [
  { key: 'code', label: __('Code'), render: (r) => <span className="font-mono">{r.code}</span>, sortValue: (r) => r.code },
  { key: 'name', label: __('Account'), render: (r) => r.name, sortValue: (r) => r.name },
  { key: 'type', label: __('Type'), render: (r) => KIND_LABEL[r.type], className: 'text-gray-500' },
  { key: 'debit', label: __('Debit'), align: 'right', render: (r) => (r.debit > 0 ? money(r.debit) : ''), sortValue: (r) => r.debit, sum: (r) => r.debit, formatSum: money },
  { key: 'credit', label: __('Credit'), align: 'right', render: (r) => (r.credit > 0 ? money(r.credit) : ''), sortValue: (r) => r.credit, sum: (r) => r.credit, formatSum: money },
];

export function TrialBalancePage() {
  const { data, loading } = useLoad(accountingApi.trialBalance, EMPTY);
  const balanced = Math.abs(data.totalDebit - data.totalCredit) < 0.005;
  const search = useSearch(data.rows, {
    text: (r) => [r.code, r.name],
    groupBys: [{ key: 'type', label: __('Type'), value: (r) => KIND_LABEL[r.type] }],
  });

  return (
    <>
      <ControlPanel
        breadcrumbs={[{ label: __('Trial balance') }]}
        exportData={() => ({ fileName: `trial-balance_${today()}`, sheets: [listSheet(__('Trial balance'), COLUMNS, search.results)] })}
        search={search}
        aside={<Badge tone={balanced ? 'green' : 'red'}>{balanced ? __('Ledger is balanced') : __('Ledger is NOT balanced')}</Badge>}
      />
      <ListView rows={search.results} columns={COLUMNS} rowKey={(r) => r.code} loading={loading} groupBy={search.groupBy} />
    </>
  );
}
