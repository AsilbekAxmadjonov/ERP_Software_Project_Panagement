import { CalendarCheck, Phone, Plus } from 'lucide-react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { money } from '../../../lib/format';
import { __ } from '../../../lib/i18n';
import { route } from '../../../lib/routes';
import { useLoad } from '../../../lib/useLoad';
import { Badge } from '../../../ui/Badge';
import { ControlPanel, type ViewMode } from '../../../ui/ControlPanel';
import { EmptyState } from '../../../ui/EmptyState';
import { Avatar, KanbanGrid } from '../../../ui/Kanban';
import { ListView, type Column } from '../../../ui/ListView';
import { useSearch } from '../../../ui/search';
import { employeesApi } from '../api';
import { PayrollRunDialog } from '../components/PayrollRunDialog';
import { STATE } from '../labels';
import { EmployeeState, type Employee } from '../types';

const balanceClass = (e: Employee) => (e.balance < 0 ? 'text-red-600' : e.balance > 0 ? 'text-gray-900' : 'text-gray-400');

const COLUMNS: readonly Column<Employee>[] = [
  {
    key: 'name',
    label: __('Name'),
    render: (e) => <span className="inline-flex items-center gap-2 font-medium"><Avatar name={e.fullName} size="sm" />{e.fullName}</span>,
    sortValue: (e) => e.fullName,
  },
  { key: 'position', label: __('Position'), render: (e) => e.position, sortValue: (e) => e.position ?? '' },
  { key: 'phone', label: __('Phone'), render: (e) => e.phone },
  { key: 'salary', label: __('Monthly salary'), align: 'right', render: (e) => money(e.monthlySalary), sortValue: (e) => e.monthlySalary, sum: (e) => e.monthlySalary, formatSum: money },
  {
    key: 'balance',
    label: __('Owed to employee'),
    align: 'right',
    render: (e) => <span className={balanceClass(e)}>{money(e.balance)}</span>,
    sortValue: (e) => e.balance,
    sum: (e) => e.balance,
    formatSum: money,
  },
  { key: 'status', label: __('Status'), render: (e) => <Badge tone={STATE[e.status].tone}>{STATE[e.status].label}</Badge> },
];

export function EmployeesPage() {
  const navigate = useNavigate();
  const [mode, setMode] = useState<ViewMode>('kanban');
  const [payroll, setPayroll] = useState(false);
  const { data: employees, loading, reload } = useLoad(employeesApi.list, []);
  const search = useSearch(employees, {
    text: (e) => [e.fullName, e.position, e.phone],
    defaultFilters: ['active'],
    filters: [
      { key: 'active', group: 'state', label: __('Active'), test: (e) => e.status === EmployeeState.ACTIVE },
      { key: 'inactive', group: 'state', label: __('Inactive'), test: (e) => e.status === EmployeeState.INACTIVE },
      { key: 'owed', group: 'balance', label: __('To pay'), test: (e) => e.balance > 0 },
      { key: 'advance', group: 'balance', label: __('Owes an advance'), test: (e) => e.balance < 0 },
    ],
    groupBys: [
      { key: 'position', label: __('Position'), value: (e) => e.position ?? '' },
      { key: 'status', label: __('Status'), value: (e) => STATE[e.status].label },
    ],
  });

  const create = () => navigate(route('employees.new'));
  const open = (e: Employee) => navigate(route('employees.show', { id: e.id }));

  return (
    <>
      <ControlPanel
        breadcrumbs={[{ label: __('Employees') }]}
        actions={
          <>
            <button className="btn" onClick={create}><Plus className="h-4 w-4" />{__('New')}</button>
            <button className="btn-secondary" onClick={() => setPayroll(true)}><CalendarCheck className="h-4 w-4" />{__('Accrue salaries')}</button>
          </>
        }
        search={search}
        view={{ mode, onChange: setMode }}
      />
      {!loading && employees.length === 0 ? (
        <EmptyState title={__('Add your first employee')} hint={__('Keep staff records, accrue salaries and track payments, advances, bonuses and deductions.')} action={<button className="btn" onClick={create}>{__('New employee')}</button>} />
      ) : mode === 'kanban' && search.groupBy === null ? (
        <KanbanGrid
          rows={search.results}
          rowKey={(e) => e.id}
          onOpen={open}
          renderCard={(e) => (
            <div className="flex gap-3">
              <Avatar name={e.fullName} size="lg" />
              <div className="min-w-0 flex-1 space-y-0.5">
                <div className="truncate font-semibold">{e.fullName}</div>
                {e.position && <div className="truncate text-xs text-gray-500">{e.position}</div>}
                {e.phone && <div className="flex items-center gap-1 truncate text-xs text-gray-600"><Phone className="h-3 w-3" />{e.phone}</div>}
                <div className="flex items-center justify-between gap-2 pt-1 text-xs">
                  {e.status === EmployeeState.INACTIVE ? <Badge tone="gray">{STATE[e.status].label}</Badge> : <span className="text-gray-500">{__('Owed')}</span>}
                  <span className={`font-semibold tabular-nums ${balanceClass(e)}`}>{money(e.balance)}</span>
                </div>
              </div>
            </div>
          )}
        />
      ) : (
        <ListView
          rows={search.results}
          columns={COLUMNS}
          rowKey={(e) => e.id}
          loading={loading}
          groupBy={search.groupBy}
          rowClassName={(e) => (e.status === EmployeeState.ACTIVE ? '' : 'text-gray-400')}
          onOpen={open}
        />
      )}
      {payroll && (
        <PayrollRunDialog
          onClose={() => setPayroll(false)}
          onDone={() => {
            setPayroll(false);
            void reload();
          }}
        />
      )}
    </>
  );
}
