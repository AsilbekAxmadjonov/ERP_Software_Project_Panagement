import { Printer } from 'lucide-react';
import { useCallback, useState } from 'react';
import { date, money, today } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { useLoad } from '../../../lib/useLoad';
import { Badge } from '../../../ui/Badge';
import { ControlPanel } from '../../../ui/ControlPanel';
import { accountingApi } from '../api';
import { ReportSheet, StatementTable, type StatementSection } from '../components/ReportSheet';
import type { BalanceSheet } from '../types';

export function BalanceSheetPage() {
  const [asOf, setAsOf] = useState(today);
  const load = useCallback(() => accountingApi.balanceSheet(asOf), [asOf]);
  const { data, loading, error } = useLoad<BalanceSheet | null>(load, null);

  const sections: StatementSection[] = data
    ? [
        { title: __('Assets'), rows: data.assets, total: { label: __('Total assets'), amount: data.totalAssets } },
        { title: __('Liabilities'), rows: data.liabilities, total: { label: __('Total liabilities'), amount: data.totalLiabilities } },
        {
          title: __('Equity'),
          rows: [...data.equity, { name: __('Current earnings (revenue − expenses, not yet closed)'), amount: data.currentEarnings }],
          total: { label: __('Total equity'), amount: data.totalEquity },
        },
      ]
    : [];

  return (
    <>
      <ControlPanel
        breadcrumbs={[{ label: __('Balance sheet') }]}
        actions={<button className="btn-secondary" onClick={() => window.print()}><Printer className="h-4 w-4" />{__('Print')}</button>}
        aside={data && (
          <Badge tone={data.balanced ? 'green' : 'red'}>
            {data.balanced ? __('Balanced: assets = liabilities + equity') : __('Not balanced')}
          </Badge>
        )}
      />
      <div className="space-y-4 p-4">
        <div className="card no-print mx-auto flex max-w-4xl flex-wrap items-end gap-3">
          <div>
            <label className="label" htmlFor="bs-as-of">{__('As of')}</label>
            <input id="bs-as-of" type="date" className="input" value={asOf} onChange={(e) => setAsOf(e.target.value)} />
          </div>
          <button type="button" className="btn-secondary" onClick={() => setAsOf(today())}>{__('Today')}</button>
        </div>
        {error && <p className="no-print mx-auto max-w-4xl text-red-600">{error}</p>}
        <ReportSheet title={__('Balance sheet')} subtitle={data ? __('As of :date').replace(':date', date(data.asOf)) : __('Loading…')} loading={loading}>
          {data && (
            <>
              <StatementTable sections={sections} results={[{ label: __('Total liabilities and equity'), amount: data.totalLiabilitiesAndEquity }]} />
              <p className={`border-t border-gray-200 px-6 py-3 text-sm ${data.balanced ? 'text-emerald-700' : 'text-red-600'}`}>
                {data.balanced
                  ? __('Check: assets :assets = liabilities and equity :total.').replace(':assets', money(data.totalAssets)).replace(':total', money(data.totalLiabilitiesAndEquity))
                  : __('Check failed: assets :assets differ from liabilities and equity :total by :diff.')
                      .replace(':assets', money(data.totalAssets))
                      .replace(':total', money(data.totalLiabilitiesAndEquity))
                      .replace(':diff', money(data.totalAssets - data.totalLiabilitiesAndEquity))}
              </p>
            </>
          )}
        </ReportSheet>
      </div>
    </>
  );
}
