import { useState } from 'react';
import { today } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { Dialog } from '../../../ui/Dialog';
import { useToast } from '../../../ui/Toast';
import { employeesApi } from '../api';

export function PayrollRunDialog({ onClose, onDone }: { onClose: () => void; onDone: () => void }) {
  const toast = useToast();
  const [period, setPeriod] = useState(today().slice(0, 7));
  const [entryDate, setEntryDate] = useState(today());
  const [processing, setProcessing] = useState(false);

  const submit = async () => {
    setProcessing(true);
    let count = 0;
    const ok = await toast.run(async () => {
      count = (await employeesApi.accrueSalaries(period, entryDate)).length;
    });
    setProcessing(false);
    if (!ok) return;
    toast.success(count === 0 ? __('Every active employee is already accrued for this month.') : __('Salary accrued for :count employees.').replace(':count', String(count)));
    onDone();
  };

  return (
    <Dialog title={__('Accrue salaries')} onClose={onClose} onSubmit={submit} submitLabel={__('Accrue')} submitDisabled={processing || period === ''}>
      <p className="text-sm text-gray-600">{__('Accrues the monthly salary of every active employee who has not been accrued for the chosen month yet.')}</p>
      <div className="grid gap-3 sm:grid-cols-2">
        <div>
          <label className="label" htmlFor="pr-period">{__('Salary month')}</label>
          <input id="pr-period" type="month" className="input" value={period} onChange={(e) => setPeriod(e.target.value)} required autoFocus />
        </div>
        <div>
          <label className="label" htmlFor="pr-date">{__('Accounting date')}</label>
          <input id="pr-date" type="date" className="input" value={entryDate} onChange={(e) => setEntryDate(e.target.value)} required />
        </div>
      </div>
    </Dialog>
  );
}
