import { Printer, Save, Undo2, Wallet } from 'lucide-react';
import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { date as formatDate, money, today } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { route } from '../../../lib/routes';
import { useLoad } from '../../../lib/useLoad';
import { Badge, Ribbon } from '../../../ui/Badge';
import { ControlPanel } from '../../../ui/ControlPanel';
import { Columns, Field, FieldGroup, Notebook, Sheet, SmartButton, SmartButtons } from '../../../ui/Form';
import { Avatar } from '../../../ui/Kanban';
import { useToast } from '../../../ui/Toast';
import { employeesApi } from '../api';
import { LedgerEntryDialog } from '../components/LedgerEntryDialog';
import { CHANNEL_LABEL, ENTRY, STATE } from '../labels';
import { EmployeeState, LedgerEntryKind, type Employee, type EmployeeInput, type LedgerRow } from '../types';

const EMPTY: EmployeeInput = { fullName: '', position: '', phone: '', monthlySalary: 0, status: EmployeeState.ACTIVE };

const toInput = (e: Employee): EmployeeInput => ({
  fullName: e.fullName,
  position: e.position ?? '',
  phone: e.phone ?? '',
  monthlySalary: e.monthlySalary,
  status: e.status,
});

const ACTIONS = [LedgerEntryKind.SALARY_ACCRUED, LedgerEntryKind.PAYMENT, LedgerEntryKind.ADVANCE, LedgerEntryKind.BONUS, LedgerEntryKind.DEDUCTION] as const;

export function EmployeeFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const toast = useToast();
  const employeeId = id === undefined ? null : Number(id);
  const loadEmployee = useCallback(() => (employeeId === null ? Promise.resolve(null) : employeesApi.get(employeeId)), [employeeId]);
  const { data: employee, error, reload } = useLoad<Employee | null>(loadEmployee, null);
  const [form, setForm] = useState<EmployeeInput>(EMPTY);
  const [salary, setSalary] = useState('');
  const [dialog, setDialog] = useState<LedgerEntryKind | null>(null);
  const [version, setVersion] = useState(0);
  const [processing, setProcessing] = useState(false);

  useEffect(() => {
    if (employee) {
      setForm(toInput(employee));
      setSalary(String(employee.monthlySalary));
    }
  }, [employee]);

  if (error !== null) return <p className="p-6 text-red-600" role="alert">{error}</p>;
  if (employeeId !== null && employee === null) return <p className="p-6 text-gray-500">{__('Loading…')}</p>;

  const current: EmployeeInput = { ...form, monthlySalary: Number(salary || 0) };
  const dirty = employee === null || JSON.stringify(toInput(employee)) !== JSON.stringify(current);

  const save = async (next: EmployeeInput = current) => {
    setProcessing(true);
    let saved: Employee | undefined;
    const ok = await toast.run(async () => {
      saved = employee === null ? await employeesApi.create(next) : await employeesApi.update(employee.id, next);
    }, __('Employee saved.'));
    setProcessing(false);
    if (!ok || !saved) return;
    if (employee === null) navigate(route('employees.show', { id: saved.id }), { replace: true });
    else await reload();
  };

  const discard = () => {
    if (employee === null) {
      navigate(route('employees.index'));
    } else {
      setForm(toInput(employee));
      setSalary(String(employee.monthlySalary));
    }
  };

  const text = (key: 'position' | 'phone', label: string, type = 'text') => (
    <Field label={label} htmlFor={`emp-${key}`}>
      <input id={`emp-${key}`} type={type} className="o-input" value={form[key]} onChange={(e) => setForm({ ...form, [key]: e.target.value })} />
    </Field>
  );

  const active = employee?.status === EmployeeState.ACTIVE;
  const canRecord = (type: LedgerEntryKind) => active || type === LedgerEntryKind.PAYMENT || type === LedgerEntryKind.DEDUCTION;

  return (
    <>
      <ControlPanel
        breadcrumbs={[{ label: __('Employees'), to: route('employees.index') }, { label: employee?.fullName ?? __('New') }]}
        actions={
          <>
            <button className="btn" disabled={!dirty || processing || form.fullName.trim() === '' || salary === ''} onClick={() => void save()}><Save className="h-4 w-4" />{__('Save')}</button>
            {dirty && <button className="btn-secondary" onClick={discard}><Undo2 className="h-4 w-4" />{__('Discard')}</button>}
          </>
        }
      />
      {employee && (
        <div className="no-print border-b border-gray-200 bg-white px-4 py-2">
          <div className="mx-auto flex max-w-6xl flex-wrap gap-2">
            {ACTIONS.filter(canRecord).map((type) => (
              <button key={type} className={type === LedgerEntryKind.SALARY_ACCRUED ? 'btn' : 'btn-secondary'} onClick={() => setDialog(type)}>{ENTRY[type].action}</button>
            ))}
            <button className="btn-secondary" disabled={processing}
              onClick={() => void save({ ...toInput(employee), status: active ? EmployeeState.INACTIVE : EmployeeState.ACTIVE })}>
              {active ? __('Deactivate') : __('Reactivate')}
            </button>
          </div>
        </div>
      )}
      <Sheet ribbon={employee && !active ? <Ribbon tone="gray">{STATE[EmployeeState.INACTIVE].label}</Ribbon> : undefined}>
        {employee && (
          <SmartButtons>
            <SmartButton icon={<Wallet className="h-5 w-5" />} value={<span className={employee.balance < 0 ? 'text-red-600' : ''}>{money(employee.balance)}</span>}
              label={employee.balance < 0 ? __('Advance to recover') : __('Owed to employee')} />
          </SmartButtons>
        )}
        <div className="mb-6 flex items-start gap-4">
          <div className="min-w-0 flex-1">
            <label htmlFor="emp-name" className="sr-only">{__('Name')}</label>
            <input id="emp-name" className="o-input text-3xl font-semibold" placeholder={__('Employee name')} value={form.fullName}
              onChange={(e) => setForm({ ...form, fullName: e.target.value })} autoFocus={employee === null} required />
          </div>
          <Avatar name={form.fullName || '?'} size="lg" />
        </div>
        <Columns>
          <FieldGroup>
            {text('position', __('Position'))}
            {text('phone', __('Phone'), 'tel')}
          </FieldGroup>
          <FieldGroup>
            <Field label={__('Monthly salary')} htmlFor="emp-salary">
              <input id="emp-salary" type="number" min="0" step="0.01" className="o-input text-right" value={salary} onChange={(e) => setSalary(e.target.value)} required />
            </Field>
            <Field label={__('Status')}><Badge tone={STATE[form.status].tone}>{STATE[form.status].label}</Badge></Field>
          </FieldGroup>
        </Columns>

        {employee && (
          <Notebook
            tabs={[
              { key: 'ledger', label: __('Ledger'), content: <LedgerTab key={version} employeeId={employee.id} /> },
              { key: 'statement', label: __('Statement'), content: <StatementTab key={version} employeeId={employee.id} /> },
            ]}
          />
        )}
      </Sheet>

      {employee && dialog !== null && (
        <LedgerEntryDialog
          employee={employee}
          type={dialog}
          onClose={() => setDialog(null)}
          onDone={() => {
            setDialog(null);
            setVersion((v) => v + 1);
            void reload();
          }}
        />
      )}
    </>
  );
}

function LedgerTable({ rows, opening, closing }: { rows: readonly LedgerRow[]; opening?: number; closing?: number }) {
  return (
    <table className="w-full">
      <thead className="border-b border-gray-300">
        <tr>
          <th className="th">{__('Date')}</th>
          <th className="th">{__('Reference')}</th>
          <th className="th">{__('Type')}</th>
          <th className="th">{__('Description')}</th>
          <th className="th text-right">{__('Earned')}</th>
          <th className="th text-right">{__('Paid / deducted')}</th>
          <th className="th text-right">{__('Balance')}</th>
        </tr>
      </thead>
      <tbody>
        {opening !== undefined && (
          <tr className="border-b border-gray-100 bg-gray-50 font-medium">
            <td className="td" colSpan={6}>{__('Opening balance')}</td>
            <td className="td text-right tabular-nums">{money(opening)}</td>
          </tr>
        )}
        {rows.length === 0 && (
          <tr><td colSpan={7} className="td py-4 text-center text-gray-500">{__('No entries in this period.')}</td></tr>
        )}
        {rows.map((r) => (
          <tr key={r.id} className="border-b border-gray-100">
            <td className="td whitespace-nowrap">{formatDate(r.date)}</td>
            <td className="td whitespace-nowrap">{r.reference}</td>
            <td className="td"><Badge tone={ENTRY[r.type].tone}>{ENTRY[r.type].label}</Badge></td>
            <td className="td text-gray-600">
              {[r.period, r.method && CHANNEL_LABEL[r.method], r.description].filter(Boolean).join(' · ')}
            </td>
            <td className="td text-right tabular-nums">{r.earned > 0 ? money(r.earned) : ''}</td>
            <td className="td text-right tabular-nums">{r.settled > 0 ? money(r.settled) : ''}</td>
            <td className={`td text-right tabular-nums ${r.balance < 0 ? 'text-red-600' : ''}`}>{money(r.balance)}</td>
          </tr>
        ))}
        {closing !== undefined && (
          <tr className="border-t-2 border-gray-300 font-semibold">
            <td className="td" colSpan={6}>{__('Closing balance')}</td>
            <td className={`td text-right tabular-nums ${closing < 0 ? 'text-red-600' : ''}`}>{money(closing)}</td>
          </tr>
        )}
      </tbody>
    </table>
  );
}

function LedgerTab({ employeeId }: { employeeId: number }) {
  const load = useCallback(() => employeesApi.ledger(employeeId), [employeeId]);
  const { data: rows, loading } = useLoad(load, []);
  if (loading && rows.length === 0) return <p className="text-gray-500">{__('Loading…')}</p>;
  return <LedgerTable rows={[...rows].reverse()} />;
}

const startOfMonth = () => `${today().slice(0, 8)}01`;

function StatementTab({ employeeId }: { employeeId: number }) {
  const [from, setFrom] = useState(startOfMonth());
  const [to, setTo] = useState(today());
  const load = useCallback(() => employeesApi.statement(employeeId, from, to), [employeeId, from, to]);
  const { data: statement, error } = useLoad(load, null);

  return (
    <div className="space-y-3">
      <div className="no-print flex flex-wrap items-end gap-3">
        <div>
          <label className="label" htmlFor="st-from">{__('From')}</label>
          <input id="st-from" type="date" className="input" value={from} onChange={(e) => setFrom(e.target.value)} />
        </div>
        <div>
          <label className="label" htmlFor="st-to">{__('To')}</label>
          <input id="st-to" type="date" className="input" value={to} onChange={(e) => setTo(e.target.value)} />
        </div>
        <button className="btn-secondary" onClick={() => window.print()}><Printer className="h-4 w-4" />{__('Print')}</button>
      </div>
      {error !== null && <p className="text-red-600" role="alert">{error}</p>}
      {statement && (
        <>
          <h3 className="font-semibold">
            {__('Statement for :name').replace(':name', statement.employee.fullName)}
            <span className="ml-2 font-normal text-gray-500">
              {[statement.from && formatDate(statement.from), statement.to && formatDate(statement.to)].filter(Boolean).join(' – ') || __('All dates')}
            </span>
          </h3>
          <LedgerTable rows={statement.rows} opening={statement.opening} closing={statement.closing} />
          <dl className="ml-auto grid w-72 grid-cols-2 gap-x-6 gap-y-1 text-sm">
            <dt className="text-right text-gray-600">{__('Total earned')}</dt>
            <dd className="text-right tabular-nums">{money(statement.totalEarned)}</dd>
            <dt className="text-right text-gray-600">{__('Total paid / deducted')}</dt>
            <dd className="text-right tabular-nums">{money(statement.totalSettled)}</dd>
          </dl>
        </>
      )}
    </div>
  );
}
