import { useState } from 'react';
import { money, today } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { Dialog } from '../../../ui/Dialog';
import { useToast } from '../../../ui/Toast';
import { employeesApi } from '../api';
import { CHANNEL_LABEL, ENTRY } from '../labels';
import { LedgerEntryKind, PaymentChannel, type Employee } from '../types';

interface Props {
  employee: Employee;
  type: LedgerEntryKind;
  onClose: () => void;
  onDone: () => void;
}

const defaultAmount = (employee: Employee, type: LedgerEntryKind): string => {
  const amounts: Record<LedgerEntryKind, number | null> = {
    [LedgerEntryKind.SALARY_ACCRUED]: employee.monthlySalary,
    [LedgerEntryKind.PAYMENT]: employee.balance > 0 ? employee.balance : null,
    [LedgerEntryKind.BONUS]: null,
    [LedgerEntryKind.ADVANCE]: null,
    [LedgerEntryKind.DEDUCTION]: null,
  };
  const amount = amounts[type];
  return amount === null ? '' : String(amount);
};

export function LedgerEntryDialog({ employee, type, onClose, onDone }: Props) {
  const toast = useToast();
  const config = ENTRY[type];
  const accrual = type === LedgerEntryKind.SALARY_ACCRUED;
  const [entryDate, setEntryDate] = useState(today());
  const [period, setPeriod] = useState(today().slice(0, 7));
  const [amount, setAmount] = useState(defaultAmount(employee, type));
  const [method, setMethod] = useState<PaymentChannel>(PaymentChannel.CASH);
  const [description, setDescription] = useState('');
  const [processing, setProcessing] = useState(false);

  const submit = async () => {
    setProcessing(true);
    const ok = await toast.run(
      () => employeesApi.record(employee.id, {
        type,
        date: entryDate,
        amount: Number(amount),
        method: config.paidOut ? method : undefined,
        period: accrual ? period : undefined,
        description,
      }),
      config.done,
    );
    setProcessing(false);
    if (ok) onDone();
  };

  return (
    <Dialog title={`${config.action} · ${employee.fullName}`} onClose={onClose} onSubmit={submit} submitLabel={__('Confirm')} submitDisabled={processing || !(Number(amount) > 0)}>
      <p className="text-sm text-gray-600">{config.hint}</p>
      <p className="text-sm">
        {__('Currently owed to the employee')}: <span className={`font-semibold tabular-nums ${employee.balance < 0 ? 'text-red-600' : ''}`}>{money(employee.balance)}</span>
      </p>
      <div className="grid gap-3 sm:grid-cols-2">
        {accrual && (
          <div>
            <label className="label" htmlFor="le-period">{__('Salary month')}</label>
            <input id="le-period" type="month" className="input" value={period} onChange={(e) => setPeriod(e.target.value)} required />
          </div>
        )}
        <div>
          <label className="label" htmlFor="le-amount">{__('Amount')}</label>
          <input id="le-amount" type="number" min="0.01" step="0.01" className="input text-right" value={amount} onChange={(e) => setAmount(e.target.value)} required autoFocus />
        </div>
        <div>
          <label className="label" htmlFor="le-date">{__('Date')}</label>
          <input id="le-date" type="date" className="input" value={entryDate} onChange={(e) => setEntryDate(e.target.value)} required />
        </div>
        {config.paidOut && (
          <fieldset className="sm:col-span-2">
            <legend className="label">{__('Journal')}</legend>
            <div className="flex gap-4">
              {Object.values(PaymentChannel).map((m) => (
                <label key={m} className="flex items-center gap-1.5">
                  <input type="radio" name="le-method" value={m} checked={method === m} onChange={() => setMethod(m)} className="accent-brand" />
                  {CHANNEL_LABEL[m]}
                </label>
              ))}
            </div>
          </fieldset>
        )}
        <div className="sm:col-span-2">
          <label className="label" htmlFor="le-description">{__('Description')}</label>
          <input id="le-description" className="input" value={description} onChange={(e) => setDescription(e.target.value)} />
        </div>
      </div>
    </Dialog>
  );
}
