import { BookOpen, FileSpreadsheet, Landmark, Lock, Plus, Scale, Users } from 'lucide-react';
import type { ReactNode } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { date, money } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { route, type RouteName } from '../../../lib/routes';
import { useLoad } from '../../../lib/useLoad';
import { Badge } from '../../../ui/Badge';
import { ControlPanel } from '../../../ui/ControlPanel';
import { Page, StatCard } from '../../../ui/EmptyState';
import { accountingApi } from '../api';
import { ORIGIN } from '../labels';
import { AccountKind, type TrialBalance } from '../types';

const EMPTY: TrialBalance = { rows: [], totalDebit: 0, totalCredit: 0 };

/** Account codes of the well-known ledger roles seeded in the chart of accounts. */
const CODE = { cash: '1000', bank: '1100', receivable: '1200', payable: '2000' } as const;

/** Debit-normal kinds show debit minus credit; the others show credit minus debit. */
const DEBIT_NORMAL: Record<AccountKind, boolean> = {
  [AccountKind.ASSET]: true,
  [AccountKind.EXPENSE]: true,
  [AccountKind.LIABILITY]: false,
  [AccountKind.EQUITY]: false,
  [AccountKind.REVENUE]: false,
};

const QUICK_LINKS: readonly { name: RouteName; label: string; icon: ReactNode }[] = [
  { name: 'accounting.journal', label: __('Journal entries'), icon: <BookOpen className="h-4 w-4" /> },
  { name: 'accounting.trial-balance', label: __('Trial balance'), icon: <Scale className="h-4 w-4" /> },
  { name: 'accounting.ledger', label: __('General ledger'), icon: <FileSpreadsheet className="h-4 w-4" /> },
  { name: 'accounting.partner-ledger', label: __('Partner ledger'), icon: <Users className="h-4 w-4" /> },
  { name: 'accounting.accounts', label: __('Chart of accounts'), icon: <Landmark className="h-4 w-4" /> },
  { name: 'accounting.lock-dates', label: __('Lock dates'), icon: <Lock className="h-4 w-4" /> },
];

export function AccountingDashboardPage() {
  const navigate = useNavigate();
  const { data: tb } = useLoad(accountingApi.trialBalance, EMPTY);
  const { data: entries } = useLoad(accountingApi.entries, []);
  const { data: lock } = useLoad(accountingApi.periodLock, null);

  const balanceOf = (code: string) => {
    const row = tb.rows.find((r) => r.code === code);
    return row ? (DEBIT_NORMAL[row.type] ? row.debit - row.credit : row.credit - row.debit) : 0;
  };
  const totalOf = (kind: AccountKind) =>
    tb.rows.filter((r) => r.type === kind).reduce((sum, r) => sum + (DEBIT_NORMAL[kind] ? r.debit - r.credit : r.credit - r.debit), 0);
  const profit = totalOf(AccountKind.REVENUE) - totalOf(AccountKind.EXPENSE);
  const recent = [...entries].sort((a, b) => b.id - a.id).slice(0, 8);
  const balanced = Math.abs(tb.totalDebit - tb.totalCredit) < 0.005;

  return (
    <>
      <ControlPanel
        breadcrumbs={[{ label: __('Accounting dashboard') }]}
        actions={<button className="btn" onClick={() => navigate(route('accounting.journal.new'))}><Plus className="h-4 w-4" />{__('New entry')}</button>}
      />
      <Page>
        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6">
          <StatCard label={__('Cash')} value={money(balanceOf(CODE.cash))} onClick={() => navigate(route('accounting.ledger'))} />
          <StatCard label={__('Bank')} value={money(balanceOf(CODE.bank))} onClick={() => navigate(route('accounting.ledger'))} />
          <StatCard label={__('Receivables')} value={money(balanceOf(CODE.receivable))} tone="warning" hint={__('Customers owe us')} onClick={() => navigate(route('accounting.partner-ledger'))} />
          <StatCard label={__('Payables')} value={money(balanceOf(CODE.payable))} tone="danger" hint={__('We owe vendors')} onClick={() => navigate(route('accounting.partner-ledger'))} />
          <StatCard label={__('Revenue')} value={money(totalOf(AccountKind.REVENUE))} tone="success" />
          <StatCard label={__('Profit')} value={money(profit)} tone={profit >= 0 ? 'success' : 'danger'} hint={__('Revenue minus expenses')} />
        </div>

        <div className="grid gap-4 lg:grid-cols-3">
          <div className="card p-0 lg:col-span-2">
            <div className="flex items-center justify-between border-b border-gray-200 px-4 py-2.5">
              <h2 className="font-semibold">{__('Recent journal entries')}</h2>
              <Link to={route('accounting.journal')} className="btn-link">{__('View all')}</Link>
            </div>
            {recent.length === 0 ? (
              <p className="p-4 text-gray-500">{__('No entries posted yet.')}</p>
            ) : (
              <table className="w-full">
                <tbody>
                  {recent.map((e) => (
                    <tr key={e.id} className="cursor-pointer border-t border-gray-100 first:border-t-0 hover:bg-gray-50" onClick={() => navigate(route('accounting.journal.show', { id: e.id }))}>
                      <td className="td text-gray-500">{date(e.entryDate)}</td>
                      <td className="td font-medium">{e.reference ?? `#${e.id}`}</td>
                      <td className="td truncate text-gray-600">{e.description}</td>
                      <td className="td"><Badge tone={ORIGIN[e.origin].tone}>{ORIGIN[e.origin].label}</Badge></td>
                      <td className="td text-right tabular-nums">{money(e.total)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>

          <div className="space-y-4">
            <div className="card">
              <h2 className="mb-2 font-semibold">{__('Ledger health')}</h2>
              <p className="flex items-center justify-between text-sm">
                {__('Trial balance')}
                <Badge tone={balanced ? 'green' : 'red'}>{balanced ? __('Balanced') : __('Not balanced')}</Badge>
              </p>
              <p className="mt-2 flex items-center justify-between text-sm">
                {__('Lock date')}
                <span className="font-medium">{lock?.lockDate ? date(lock.lockDate) : __('None')}</span>
              </p>
            </div>
            <div className="card">
              <h2 className="mb-2 font-semibold">{__('Shortcuts')}</h2>
              <ul className="space-y-1">
                {QUICK_LINKS.map((l) => (
                  <li key={l.name}>
                    <Link to={route(l.name)} className="flex items-center gap-2 rounded px-2 py-1 text-accent hover:bg-gray-50">{l.icon}{l.label}</Link>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </div>
      </Page>
    </>
  );
}
