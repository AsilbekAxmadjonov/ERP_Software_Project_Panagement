import { Trash2, Undo2 } from 'lucide-react';
import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { date as formatDate, money, today } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { route } from '../../../lib/routes';
import { useLoad } from '../../../lib/useLoad';
import { Badge, Ribbon } from '../../../ui/Badge';
import { ControlPanel } from '../../../ui/ControlPanel';
import { Columns, Field, FieldGroup, Notebook, Sheet, SheetTitle, StatusBar, Totals } from '../../../ui/Form';
import { Many2One } from '../../../ui/Many2One';
import { useToast } from '../../../ui/Toast';
import { accountingApi } from '../api';
import { ORIGIN } from '../labels';
import { EntryKind, type JournalEntry } from '../types';

type Stage = 'draft' | 'posted' | 'reversed';

const STAGES: readonly { key: Stage; label: string }[] = [
  { key: 'draft', label: __('Draft') },
  { key: 'posted', label: __('Posted') },
];

export function JournalEntryFormPage() {
  const { id } = useParams();
  return id === undefined ? <NewEntry /> : <ExistingEntry id={Number(id)} />;
}

interface LineDraft {
  key: number;
  accountCode: string | null;
  debit: string;
  credit: string;
  memo: string;
}

let lineKey = 1;
const emptyLine = (): LineDraft => ({ key: lineKey++, accountCode: null, debit: '', credit: '', memo: '' });
const num = (value: string) => (value === '' ? 0 : Number(value));

function NewEntry() {
  const navigate = useNavigate();
  const toast = useToast();
  const { data: accounts } = useLoad(accountingApi.accounts, []);
  const [entryDate, setEntryDate] = useState(today());
  const [reference, setReference] = useState('');
  const [description, setDescription] = useState('');
  const [lines, setLines] = useState<LineDraft[]>([emptyLine(), emptyLine()]);
  const [processing, setProcessing] = useState(false);

  // Many2One works with numeric ids; accounts are keyed by code, so map through the account id.
  const active = accounts.filter((a) => a.active);
  const options = active.map((a) => ({ value: a.id, label: `${a.code} ${a.name}` }));
  const idOf = (code: string | null) => active.find((a) => a.code === code)?.id ?? null;
  const codeOf = (id: number | null) => active.find((a) => a.id === id)?.code ?? null;

  const totalDebit = lines.reduce((s, l) => s + num(l.debit), 0);
  const totalCredit = lines.reduce((s, l) => s + num(l.credit), 0);
  const difference = totalDebit - totalCredit;
  const balanced = totalDebit > 0 && Math.abs(difference) < 0.005;
  const filled = lines.filter((l) => l.accountCode !== null && (num(l.debit) > 0 || num(l.credit) > 0));

  const setLine = (key: number, patch: Partial<LineDraft>) => setLines(lines.map((l) => (l.key === key ? { ...l, ...patch } : l)));

  /** Like Odoo, a new line proposes the amount that would balance the entry. */
  const addLine = () => {
    const line = emptyLine();
    if (difference > 0.005) line.credit = difference.toFixed(2);
    else if (difference < -0.005) line.debit = (-difference).toFixed(2);
    setLines([...lines, line]);
  };

  const post = async () => {
    setProcessing(true);
    let created: JournalEntry | undefined;
    const ok = await toast.run(async () => {
      created = await accountingApi.postEntry({
        date: entryDate,
        reference,
        description,
        lines: filled.map((l) => ({ accountCode: l.accountCode!, debit: num(l.debit), credit: num(l.credit), memo: l.memo })),
      });
    });
    setProcessing(false);
    if (ok && created) {
      toast.success(__('Journal entry posted.'));
      navigate(route('accounting.journal.show', { id: created.id }), { replace: true });
    }
  };

  return (
    <>
      <ControlPanel breadcrumbs={[{ label: __('Journal entries'), to: route('accounting.journal') }, { label: __('New') }]} />
      <StatusBar stages={STAGES} current="draft">
        <button className="btn" onClick={() => void post()} disabled={!balanced || processing}>{__('Post')}</button>
        <button className="btn-secondary" onClick={() => navigate(route('accounting.journal'))}>{__('Discard')}</button>
      </StatusBar>
      <Sheet>
        <SheetTitle overline={__('Journal entry')}><span className="text-gray-400">{__('Draft')}</span></SheetTitle>
        <Columns>
          <FieldGroup>
            <Field label={__('Reference')} htmlFor="je-ref">
              <input id="je-ref" className="o-input" value={reference} onChange={(e) => setReference(e.target.value)} autoFocus />
            </Field>
            <Field label={__('Description')} htmlFor="je-desc">
              <input id="je-desc" className="o-input" value={description} onChange={(e) => setDescription(e.target.value)} />
            </Field>
          </FieldGroup>
          <FieldGroup>
            <Field label={__('Accounting date')} htmlFor="je-date">
              <input id="je-date" type="date" className="o-input" value={entryDate} onChange={(e) => setEntryDate(e.target.value)} />
            </Field>
          </FieldGroup>
        </Columns>
        <Notebook
          tabs={[{
            key: 'lines',
            label: __('Journal items'),
            content: (
              <>
                <table className="w-full">
                  <thead className="border-b border-gray-300">
                    <tr>
                      <th className="th w-2/5">{__('Account')}</th>
                      <th className="th">{__('Label')}</th>
                      <th className="th text-right">{__('Debit')}</th>
                      <th className="th text-right">{__('Credit')}</th>
                      <th className="th w-8" />
                    </tr>
                  </thead>
                  <tbody>
                    {lines.map((l) => (
                      <tr key={l.key} className="border-b border-gray-100">
                        <td className="td">
                          <Many2One aria-label={__('Account')} placeholder={__('Select an account…')} options={options} value={idOf(l.accountCode)} onChange={(v) => setLine(l.key, { accountCode: codeOf(v) })} />
                        </td>
                        <td className="td"><input aria-label={__('Label')} className="o-input" value={l.memo} onChange={(e) => setLine(l.key, { memo: e.target.value })} /></td>
                        <td className="td"><input aria-label={__('Debit')} type="number" min="0" step="0.01" className="o-input text-right" value={l.debit} onChange={(e) => setLine(l.key, { debit: e.target.value, credit: '' })} /></td>
                        <td className="td"><input aria-label={__('Credit')} type="number" min="0" step="0.01" className="o-input text-right" value={l.credit} onChange={(e) => setLine(l.key, { credit: e.target.value, debit: '' })} /></td>
                        <td className="td">
                          <button type="button" className="text-gray-400 hover:text-red-600" aria-label={__('Remove line')} disabled={lines.length <= 2} onClick={() => setLines(lines.filter((x) => x.key !== l.key))}>
                            <Trash2 className="h-4 w-4" />
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                  <tfoot className="font-semibold">
                    <tr>
                      <td className="td" colSpan={2}>
                        {balanced ? <Badge tone="green">{__('Balanced')}</Badge> : <Badge tone="yellow">{__('Unbalanced by :amount').replace(':amount', money(Math.abs(difference)))}</Badge>}
                      </td>
                      <td className="td text-right tabular-nums">{money(totalDebit)}</td>
                      <td className="td text-right tabular-nums">{money(totalCredit)}</td>
                      <td />
                    </tr>
                  </tfoot>
                </table>
                <button type="button" className="btn-link mt-2 px-3" onClick={addLine}>{__('Add a line')}</button>
              </>
            ),
          }]}
        />
      </Sheet>
    </>
  );
}

function ExistingEntry({ id }: { id: number }) {
  const toast = useToast();
  const navigate = useNavigate();
  const { data: entries, loading, reload } = useLoad(accountingApi.entries, []);
  const [processing, setProcessing] = useState(false);
  const entry = entries.find((e) => e.id === id);

  if (loading && entry === undefined) return <p className="p-6 text-gray-500">{__('Loading…')}</p>;
  if (entry === undefined) return <p className="p-6 text-red-600" role="alert">{__('Journal entry not found.')}</p>;

  const reversal = entries.find((e) => e.reversalOf === entry.id);
  const original = entry.reversalOf === null ? undefined : entries.find((e) => e.id === entry.reversalOf);
  const debit = entry.lines.reduce((s, l) => s + l.debit, 0);
  const credit = entry.lines.reduce((s, l) => s + l.credit, 0);

  const reverse = async () => {
    setProcessing(true);
    let created: JournalEntry | undefined;
    const ok = await toast.run(async () => {
      created = await accountingApi.reverseEntry(entry.id);
    }, __('Reversal entry posted.'));
    setProcessing(false);
    if (ok && created) {
      await reload();
      navigate(route('accounting.journal.show', { id: created.id }));
    }
  };

  return (
    <>
      <ControlPanel breadcrumbs={[{ label: __('Journal entries'), to: route('accounting.journal') }, { label: entry.reference ?? `#${entry.id}` }]} />
      <StatusBar stages={STAGES} current="posted">
        {entry.origin === EntryKind.MANUAL && !entry.reversed && (
          <button className="btn-secondary" onClick={() => void reverse()} disabled={processing}><Undo2 className="h-4 w-4" />{__('Reverse entry')}</button>
        )}
      </StatusBar>
      <Sheet ribbon={entry.reversed ? <Ribbon tone="red">{__('Reversed')}</Ribbon> : undefined}>
        <SheetTitle overline={__('Journal entry')}>{entry.reference ?? `#${entry.id}`}</SheetTitle>
        <Columns>
          <FieldGroup>
            <Field label={__('Description')}>{entry.description}</Field>
            <Field label={__('Source')}><Badge tone={ORIGIN[entry.origin].tone}>{ORIGIN[entry.origin].label}</Badge></Field>
            {reversal && (
              <Field label={__('Reversed by')}><Link className="text-accent hover:underline" to={route('accounting.journal.show', { id: reversal.id })}>{reversal.reference ?? `#${reversal.id}`}</Link></Field>
            )}
            {original && (
              <Field label={__('Reversal of')}><Link className="text-accent hover:underline" to={route('accounting.journal.show', { id: original.id })}>{original.reference ?? `#${original.id}`}</Link></Field>
            )}
          </FieldGroup>
          <FieldGroup>
            <Field label={__('Accounting date')}>{formatDate(entry.entryDate)}</Field>
            <Field label={__('Created by')}>{entry.createdBy}</Field>
          </FieldGroup>
        </Columns>
        <Notebook
          tabs={[{
            key: 'lines',
            label: __('Journal items'),
            content: (
              <>
                <table className="w-full">
                  <thead className="border-b border-gray-300">
                    <tr><th className="th">{__('Account')}</th><th className="th">{__('Label')}</th><th className="th text-right">{__('Debit')}</th><th className="th text-right">{__('Credit')}</th></tr>
                  </thead>
                  <tbody>
                    {entry.lines.map((l, i) => (
                      <tr key={i} className="border-b border-gray-100">
                        <td className="td"><span className="font-mono text-gray-500">{l.accountCode}</span> {l.accountName}</td>
                        <td className="td text-gray-600">{l.memo}</td>
                        <td className="td text-right tabular-nums">{l.debit > 0 ? money(l.debit) : ''}</td>
                        <td className="td text-right tabular-nums">{l.credit > 0 ? money(l.credit) : ''}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                <Totals rows={[{ label: __('Debit'), value: money(debit) }, { label: __('Credit'), value: money(credit) }]} />
              </>
            ),
          }]}
        />
      </Sheet>
    </>
  );
}
