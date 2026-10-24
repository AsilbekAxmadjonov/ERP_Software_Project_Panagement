import { Lock, LockOpen } from 'lucide-react';
import { useState } from 'react';
import { date, today } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { useLoad } from '../../../lib/useLoad';
import { ControlPanel } from '../../../ui/ControlPanel';
import { Field, FieldGroup, Sheet, SheetTitle } from '../../../ui/Form';
import { useToast } from '../../../ui/Toast';
import { accountingApi } from '../api';
import type { PeriodLock } from '../types';

export function LockDatesPage() {
  const toast = useToast();
  const { data: lock, loading, reload } = useLoad<PeriodLock | null>(accountingApi.periodLock, null);
  const [lockDate, setLockDate] = useState(today());
  const [processing, setProcessing] = useState(false);

  const act = async (action: () => Promise<unknown>, message: string) => {
    setProcessing(true);
    if (await toast.run(action, message)) await reload();
    setProcessing(false);
  };

  return (
    <>
      <ControlPanel breadcrumbs={[{ label: __('Lock dates') }]} />
      <Sheet>
        <SheetTitle overline={__('Period closing')}>{__('Lock dates')}</SheetTitle>
        <p className="mb-6 max-w-2xl text-gray-600">
          {__('Once a period is closed, no journal entry dated on or before the lock date can be posted or reversed — including invoices, receipts and payments. Only the CEO can reopen a closed period.')}
        </p>
        <div className="grid gap-8 md:grid-cols-2">
          <FieldGroup title={__('Current status')}>
            <Field label={__('Locked until')}>
              {loading && lock === null ? __('Loading…') : lock?.lockDate ? (
                <span className="inline-flex items-center gap-1.5 font-medium"><Lock className="h-4 w-4 text-brand" />{date(lock.lockDate)}</span>
              ) : (
                <span className="inline-flex items-center gap-1.5 text-gray-500"><LockOpen className="h-4 w-4" />{__('No period is closed')}</span>
              )}
            </Field>
            {lock?.changedBy && <Field label={__('Last changed by')}>{lock.changedBy}</Field>}
            {lock?.changedAt && <Field label={__('Changed on')}>{new Date(lock.changedAt).toLocaleString()}</Field>}
          </FieldGroup>
          <FieldGroup title={__('Close a period')}>
            <Field label={__('Lock date')} htmlFor="lock-date">
              <input id="lock-date" type="date" className="o-input" value={lockDate} onChange={(e) => setLockDate(e.target.value)} />
            </Field>
            <div />
            <div className="flex flex-wrap gap-2">
              <button className="btn" disabled={processing || lockDate === ''} onClick={() => void act(() => accountingApi.closePeriod(lockDate), __('Period closed.'))}>
                <Lock className="h-4 w-4" />{__('Close period')}
              </button>
              {lock?.lockDate && (
                <button className="btn-secondary" disabled={processing} onClick={() => void act(() => accountingApi.reopenPeriod(null), __('All periods reopened.'))}>
                  <LockOpen className="h-4 w-4" />{__('Reopen')}
                </button>
              )}
            </div>
          </FieldGroup>
        </div>
      </Sheet>
    </>
  );
}
