import { useCallback, useState } from 'react';
import { __ } from '../../../lib/i18n';
import { useLoad } from '../../../lib/useLoad';
import { ControlPanel } from '../../../ui/ControlPanel';
import { Many2One } from '../../../ui/Many2One';
import { accountingApi } from '../api';
import { LedgerView, ledgerSheet } from '../components/LedgerView';
import type { LedgerRow } from '../types';

export function PartnerLedgerPage() {
  const { data: partners } = useLoad(accountingApi.partnerOptions, []);
  const [partnerId, setPartnerId] = useState<number | null>(null);
  const loadLedger = useCallback(() => (partnerId === null ? Promise.resolve<LedgerRow[]>([]) : accountingApi.partnerLedger(partnerId)), [partnerId]);
  const { data: rows, loading } = useLoad(loadLedger, []);
  const partner = partners.find((p) => p.id === partnerId);

  return (
    <>
      <ControlPanel
        breadcrumbs={[{ label: __('Partner ledger') }]}
        exportData={partner === undefined ? undefined : () => ({ fileName: `partner-ledger_${partner.name}`, sheets: [ledgerSheet(partner.name, rows)] })}
        aside={
          <div className="w-80">
            <Many2One
              aria-label={__('Partner')}
              variant="boxed"
              placeholder={__('Select a partner…')}
              options={partners.map((p) => ({ value: p.id, label: p.name }))}
              value={partnerId}
              onChange={setPartnerId}
            />
          </div>
        }
      />
      <LedgerView rows={rows} loading={loading} selected={partnerId !== null} />
    </>
  );
}
