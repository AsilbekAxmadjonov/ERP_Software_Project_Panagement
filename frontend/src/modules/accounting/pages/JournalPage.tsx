import { Fragment, useState, type FormEvent } from 'react';
import { ApiError } from '../../../lib/http';
import { money, today } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { useLoad } from '../../../lib/useLoad';
import { accountingApi } from '../api';
import { EntryKind } from '../types';

const ORIGIN_LABEL: Record<EntryKind, string> = {
  [EntryKind.MANUAL]: __('Manual'),
  [EntryKind.SALE]: __('Sale'),
  [EntryKind.PAYMENT]: __('Payment'),
  [EntryKind.STOCK_ADJUSTMENT]: __('Stock adjustment'),
  [EntryKind.REVERSAL]: __('Reversal'),
};

interface LineDraft {
  accountCode: string;
  debit: string;
  credit: string;
  memo: string;
}

const emptyLine = (): LineDraft => ({ accountCode: '', debit: '', credit: '', memo: '' });
const num = (value: string) => (value === '' ? 0 : Number(value));

export function JournalPage() {
  const { data: entries, error: loadError, reload } = useLoad(accountingApi.entries, []);
  const { data: accounts } = useLoad(accountingApi.accounts, []);
  const [date, setDate] = useState(today());
  const [reference, setReference] = useState('');
  const [description, setDescription] = useState('');
  const [lines, setLines] = useState<LineDraft[]>([emptyLine(), emptyLine()]);
  const [error, setError] = useState<string | null>(null);
  const [open, setOpen] = useState<number | null>(null);

  const totalDebit = lines.reduce((sum, l) => sum + num(l.debit), 0);
  const totalCredit = lines.reduce((sum, l) => sum + num(l.credit), 0);
  const balanced = totalDebit > 0 && Math.abs(totalDebit - totalCredit) < 0.005;

  const setLine = (index: number, patch: Partial<LineDraft>) =>
    setLines(lines.map((line, i) => (i === index ? { ...line, ...patch } : line)));

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    try {
      await accountingApi.postEntry({
        date,
        reference,
        description,
        lines: lines.map((l) => ({ accountCode: l.accountCode, debit: num(l.debit), credit: num(l.credit), memo: l.memo })),
      });
      setReference('');
      setDescription('');
      setLines([emptyLine(), emptyLine()]);
      await reload();
    } catch (e) {
      setError(e instanceof ApiError ? e.message : __('Something went wrong.'));
    }
  };

  const reverse = async (id: number) => {
    setError(null);
    try {
      await accountingApi.reverseEntry(id);
      await reload();
    } catch (e) {
      setError(e instanceof ApiError ? e.message : __('Something went wrong.'));
    }
  };

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold">{__('Journal entries')}</h1>

      <form onSubmit={submit} className="card space-y-3">
        <div className="grid gap-3 sm:grid-cols-3">
          <div>
            <label className="label" htmlFor="j-date">{__('Date')}</label>
            <input id="j-date" type="date" className="input" value={date} onChange={(e) => setDate(e.target.value)} required />
          </div>
          <div>
            <label className="label" htmlFor="j-ref">{__('Reference')}</label>
            <input id="j-ref" className="input" value={reference} onChange={(e) => setReference(e.target.value)} />
          </div>
          <div>
            <label className="label" htmlFor="j-desc">{__('Description')}</label>
            <input id="j-desc" className="input" value={description} onChange={(e) => setDescription(e.target.value)} />
          </div>
        </div>

        {lines.map((line, index) => (
          <div key={index} className="grid gap-2 sm:grid-cols-[2fr_1fr_1fr_2fr_auto]">
            <select aria-label={__('Account')} className="input" value={line.accountCode} onChange={(e) => setLine(index, { accountCode: e.target.value })} required>
              <option value="">{__('Select account…')}</option>
              {accounts.filter((a) => a.active).map((a) => (
                <option key={a.code} value={a.code}>{a.code} · {a.name}</option>
              ))}
            </select>
            <input aria-label={__('Debit')} placeholder={__('Debit')} type="number" min="0" step="0.01" className="input" value={line.debit} onChange={(e) => setLine(index, { debit: e.target.value, credit: '' })} />
            <input aria-label={__('Credit')} placeholder={__('Credit')} type="number" min="0" step="0.01" className="input" value={line.credit} onChange={(e) => setLine(index, { credit: e.target.value, debit: '' })} />
            <input aria-label={__('Memo')} placeholder={__('Memo')} className="input" value={line.memo} onChange={(e) => setLine(index, { memo: e.target.value })} />
            <button type="button" className="btn-secondary" disabled={lines.length <= 2} onClick={() => setLines(lines.filter((_, i) => i !== index))}>×</button>
          </div>
        ))}

        <div className="flex flex-wrap items-center justify-between gap-3">
          <button type="button" className="btn-secondary" onClick={() => setLines([...lines, emptyLine()])}>{__('Add line')}</button>
          <div className="flex items-center gap-4 text-sm">
            <span>{__('Debit')}: <b>{money(totalDebit)}</b></span>
            <span>{__('Credit')}: <b>{money(totalCredit)}</b></span>
            <span className={balanced ? 'text-green-700' : 'text-amber-700'}>{balanced ? __('Balanced') : __('Not balanced')}</span>
            <button className="btn" disabled={!balanced}>{__('Post entry')}</button>
          </div>
        </div>
        {(error ?? loadError) !== null && <p role="alert" className="text-sm text-red-600">{error ?? loadError}</p>}
      </form>

      <div className="card overflow-x-auto p-0">
        <table className="min-w-full divide-y divide-slate-200">
          <thead>
            <tr>
              <th className="th">#</th><th className="th">{__('Date')}</th><th className="th">{__('Reference')}</th>
              <th className="th">{__('Description')}</th><th className="th">{__('Source')}</th>
              <th className="th text-right">{__('Amount')}</th><th className="th" />
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {entries.map((e) => (
              <Fragment key={e.id}>
                <tr className={e.reversed ? 'text-slate-400 line-through' : ''}>
                  <td className="td"><button className="underline" onClick={() => setOpen(open === e.id ? null : e.id)}>{e.id}</button></td>
                  <td className="td">{e.entryDate}</td>
                  <td className="td">{e.reference}</td>
                  <td className="td">{e.description}</td>
                  <td className="td">{ORIGIN_LABEL[e.origin]}</td>
                  <td className="td text-right">{money(e.total)}</td>
                  <td className="td text-right">
                    {e.origin === EntryKind.MANUAL && !e.reversed && (
                      <button className="btn-secondary" onClick={() => void reverse(e.id)}>{__('Reverse')}</button>
                    )}
                  </td>
                </tr>
                {open === e.id && (
                  <tr>
                    <td colSpan={7} className="bg-slate-50 px-6 py-2">
                      {e.lines.map((l, i) => (
                        <div key={i} className="grid grid-cols-[3fr_1fr_1fr_2fr] gap-2 text-sm">
                          <span>{l.accountCode} · {l.accountName}</span>
                          <span className="text-right">{l.debit > 0 ? money(l.debit) : ''}</span>
                          <span className="text-right">{l.credit > 0 ? money(l.credit) : ''}</span>
                          <span className="text-slate-500">{l.memo}</span>
                        </div>
                      ))}
                    </td>
                  </tr>
                )}
              </Fragment>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
