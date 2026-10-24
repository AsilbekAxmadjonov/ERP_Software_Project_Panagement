import { Printer } from 'lucide-react';
import { useCallback, useState } from 'react';
import { useParams } from 'react-router-dom';
import { date as formatDate, money, today } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { route, type RouteName } from '../../../lib/routes';
import { useLoad } from '../../../lib/useLoad';
import { Badge, Ribbon } from '../../../ui/Badge';
import { ControlPanel } from '../../../ui/ControlPanel';
import { Dialog } from '../../../ui/Dialog';
import { Columns, Field, FieldGroup, Sheet, SheetTitle, StatusBar } from '../../../ui/Form';
import { useToast } from '../../../ui/Toast';
import { expensesApi } from '../api';
import { CHANNEL_LABEL, STATE, TYPE } from '../labels';
import { EntryState, EntryType, type CashEntry } from '../types';

const LIST_ROUTE: Record<EntryType, RouteName> = {
  [EntryType.EXPENSE]: 'expenses.expenses',
  [EntryType.INCOME]: 'expenses.income',
};

const STAGES = Object.values(EntryState).map((s) => ({ key: s, label: STATE[s].label }));

export function CashEntryFormPage() {
  const { id } = useParams();
  const toast = useToast();
  const loadEntry = useCallback(() => expensesApi.entry(Number(id)), [id]);
  const { data: entry, error, reload } = useLoad<CashEntry | null>(loadEntry, null);
  const [cancelling, setCancelling] = useState(false);
  const [cancelDate, setCancelDate] = useState(today());
  const [processing, setProcessing] = useState(false);

  if (error !== null) return <p className="p-6 text-red-600" role="alert">{error}</p>;
  if (entry === null) return <p className="p-6 text-gray-500">{__('Loading…')}</p>;

  const cancel = async () => {
    setProcessing(true);
    const ok = await toast.run(() => expensesApi.cancel(entry.id, cancelDate), __(':number cancelled and reversed in the ledger.').replace(':number', entry.number));
    setProcessing(false);
    if (ok) {
      setCancelling(false);
      await reload();
    }
  };

  return (
    <>
      <ControlPanel breadcrumbs={[{ label: TYPE[entry.type].plural, to: route(LIST_ROUTE[entry.type]) }, { label: entry.number }]} />
      <StatusBar stages={STAGES} current={entry.status}>
        {entry.status === EntryState.POSTED && (
          <button className="btn-secondary" disabled={processing} onClick={() => setCancelling(true)}>{__('Cancel entry')}</button>
        )}
        <button className="btn-secondary" onClick={() => window.print()}><Printer className="h-4 w-4" />{__('Print')}</button>
      </StatusBar>
      <Sheet ribbon={entry.status === EntryState.CANCELLED ? <Ribbon tone="gray">{__('Cancelled')}</Ribbon> : undefined}>
        <SheetTitle overline={TYPE[entry.type].overline}>{entry.number}</SheetTitle>
        <Columns>
          <FieldGroup>
            <Field label={__('Category')}><span className="font-medium text-accent">{entry.categoryName}</span></Field>
            <Field label={__('Ledger account')}>{entry.accountCode}</Field>
            <Field label={__('Description')}>{entry.description ?? <span className="text-gray-400">{__('None')}</span>}</Field>
          </FieldGroup>
          <FieldGroup>
            <Field label={__('Date')}>{formatDate(entry.date)}</Field>
            <Field label={__('Amount')}><span className="text-lg font-semibold tabular-nums">{money(entry.amount)}</span></Field>
            <Field label={__('Journal')}>{CHANNEL_LABEL[entry.method]}</Field>
            <Field label={__('Recorded by')}>{entry.createdBy}</Field>
            <Field label={__('Status')}><Badge tone={STATE[entry.status].tone}>{STATE[entry.status].label}</Badge></Field>
            {entry.cancelledOn && <Field label={__('Cancelled on')}>{formatDate(entry.cancelledOn)}</Field>}
          </FieldGroup>
        </Columns>
      </Sheet>
      {cancelling && (
        <Dialog title={__('Cancel entry')} onClose={() => setCancelling(false)} onSubmit={cancel} submitLabel={__('Cancel entry')} submitDisabled={processing}>
          <p className="text-gray-600">{__('A reversing journal entry will be posted on the cancellation date, which must be in an open period.')}</p>
          <div>
            <label className="label" htmlFor="ce-cancel-date">{__('Cancellation date')}</label>
            <input id="ce-cancel-date" type="date" className="input" min={entry.date} value={cancelDate} onChange={(e) => setCancelDate(e.target.value)} required />
          </div>
        </Dialog>
      )}
    </>
  );
}
