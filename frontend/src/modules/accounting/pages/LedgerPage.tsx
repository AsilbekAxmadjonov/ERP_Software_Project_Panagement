import { useCallback, useState } from 'react';
import { __ } from '../../../lib/i18n';
import { useLoad } from '../../../lib/useLoad';
import { ControlPanel } from '../../../ui/ControlPanel';
import { Many2One } from '../../../ui/Many2One';
import { accountingApi } from '../api';
import { LedgerView } from '../components/LedgerView';
import type { LedgerRow } from '../types';

export function LedgerPage() {
  const { data: accounts } = useLoad(accountingApi.accounts, []);
  const [accountId, setAccountId] = useState<number | null>(null);
  const code = accounts.find((a) => a.id === accountId)?.code ?? null;
  const loadLedger = useCallback(() => (code === null ? Promise.resolve<LedgerRow[]>([]) : accountingApi.accountLedger(code)), [code]);
  const { data: rows, loading } = useLoad(loadLedger, []);

  return (
    <>
      <ControlPanel
        breadcrumbs={[{ label: __('General ledger') }]}
        aside={
          <div className="w-80">
            <Many2One
              aria-label={__('Account')}
              variant="boxed"
              placeholder={__('Select an account…')}
              options={accounts.map((a) => ({ value: a.id, label: `${a.code} ${a.name}` }))}
              value={accountId}
              onChange={setAccountId}
            />
          </div>
        }
      />
      <LedgerView rows={rows} loading={loading} selected={code !== null} />
    </>
  );
}
