import { useCallback, useState } from 'react';
import { date } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { useLoad } from '../../../lib/useLoad';
import { ControlPanel } from '../../../ui/ControlPanel';
import { accountingApi } from '../api';
import { DateRangeFilter, ReportSheet, StatementTable, statementSheet, type StatementSection } from '../components/ReportSheet';
import { PERIOD_PRESETS } from '../periods';
import type { ProfitAndLoss } from '../types';

export function ProfitLossPage() {
  const [range, setRange] = useState(PERIOD_PRESETS.year.range);
  const load = useCallback(() => accountingApi.profitAndLoss(range.from, range.to), [range]);
  const { data, loading, error } = useLoad<ProfitAndLoss | null>(load, null);

  const sections: StatementSection[] = data
    ? [
        { title: __('Income'), rows: data.revenue, empty: __('No income in this period'), total: { label: __('Total income'), amount: data.totalRevenue } },
        { title: __('Expenses'), rows: data.expenses, empty: __('No expenses in this period'), total: { label: __('Total expenses'), amount: data.totalExpenses } },
      ]
    : [];

  return (
    <>
      <ControlPanel
        breadcrumbs={[{ label: __('Profit and loss') }]}
        printTitle={false}
        exportData={() => ({
          fileName: `profit-and-loss_${data?.from ?? range.from}_${data?.to ?? range.to}`,
          sheets: [statementSheet(__('Profit and loss'), sections, data ? [{ label: __('Net profit'), amount: data.netProfit }] : [])],
        })}
      />
      <div className="space-y-4 p-4">
        <DateRangeFilter value={range} onChange={setRange} />
        {error && <p className="no-print mx-auto max-w-4xl text-red-600">{error}</p>}
        <ReportSheet
          title={__('Profit and loss')}
          subtitle={data ? __('From :from to :to').replace(':from', date(data.from)).replace(':to', date(data.to)) : __('Loading…')}
          loading={loading}
        >
          {data && <StatementTable sections={sections} results={[{ label: __('Net profit'), amount: data.netProfit }]} />}
        </ReportSheet>
      </div>
    </>
  );
}
