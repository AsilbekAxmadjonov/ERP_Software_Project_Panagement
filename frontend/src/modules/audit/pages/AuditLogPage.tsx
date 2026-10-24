import { useCallback, useState } from 'react';
import { __ } from '../../../lib/i18n';
import { useLoad } from '../../../lib/useLoad';
import { Badge } from '../../../ui/Badge';
import { ControlPanel } from '../../../ui/ControlPanel';
import { Dialog } from '../../../ui/Dialog';
import { ListView, type Column } from '../../../ui/ListView';
import { useSearch, type FilterDef } from '../../../ui/search';
import { auditApi } from '../api';
import { actionGroup, actionLabel, groupLabel, groupTone } from '../labels';
import type { AuditEntry, AuditFilter } from '../types';

const NO_FILTER: AuditFilter = { from: '', to: '' };
const LOGIN_FAILED = 'identity.login-failed';

const DATE_TIME = new Intl.DateTimeFormat('en-GB', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit', second: '2-digit' });

const dateTime = (iso: string) => DATE_TIME.format(new Date(iso));

/** The local calendar day, so grouping by day matches what the user sees in the date column. */
const localDay = (iso: string) => {
  const d = new Date(iso);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
};

const NO_ACTOR = __('system');

const COLUMNS: readonly Column<AuditEntry>[] = [
  { key: 'when', label: __('Date'), render: (e) => <span className="whitespace-nowrap tabular-nums">{dateTime(e.occurredAt)}</span>, sortValue: (e) => e.occurredAt },
  { key: 'actor', label: __('User'), render: (e) => e.actor ?? NO_ACTOR, sortValue: (e) => e.actor ?? '' },
  {
    key: 'group',
    label: __('Area'),
    render: (e) => <Badge tone={groupTone(actionGroup(e.action))}>{groupLabel(actionGroup(e.action))}</Badge>,
    sortValue: (e) => actionGroup(e.action),
  },
  {
    key: 'action',
    label: __('Action'),
    render: (e) => <span className={e.action === LOGIN_FAILED ? 'font-medium text-red-700' : ''}>{actionLabel(e.action)}</span>,
    sortValue: (e) => e.action,
  },
  { key: 'subject', label: __('Reference'), render: (e) => <span className="font-medium">{e.subject}</span>, sortValue: (e) => e.subject ?? '' },
  { key: 'summary', label: __('Summary'), render: (e) => e.summary, className: 'text-gray-600' },
];

export function AuditLogPage() {
  const [filter, setFilter] = useState<AuditFilter>(NO_FILTER);
  const [open, setOpen] = useState<AuditEntry | null>(null);
  const load = useCallback(() => auditApi.entries(filter), [filter]);
  const { data: entries, loading, error } = useLoad(load, []);

  const groups = [...new Set(entries.map((e) => actionGroup(e.action)))].sort();
  const groupFilters: FilterDef<AuditEntry>[] = groups.map((g) => ({
    key: `group:${g}`,
    group: 'area',
    label: groupLabel(g),
    test: (e) => actionGroup(e.action) === g,
  }));

  const search = useSearch(entries, {
    text: (e) => [e.summary, e.actor, e.action, e.subject],
    filters: [
      ...groupFilters,
      { key: 'failed-logins', group: 'security', label: __('Failed logins'), test: (e) => e.action === LOGIN_FAILED },
    ],
    groupBys: [
      { key: 'actor', label: __('User'), value: (e) => e.actor ?? NO_ACTOR },
      { key: 'action', label: __('Action'), value: (e) => `${groupLabel(actionGroup(e.action))}: ${actionLabel(e.action)}` },
      { key: 'group', label: __('Area'), value: (e) => groupLabel(actionGroup(e.action)) },
      { key: 'day', label: __('Day'), value: (e) => localDay(e.occurredAt) },
    ],
  });

  return (
    <>
      <ControlPanel
        breadcrumbs={[{ label: __('Audit log') }]}
        search={search}
        aside={
          <div className="flex flex-wrap items-center gap-2 text-sm">
            <label className="sr-only" htmlFor="audit-from">{__('From')}</label>
            <input id="audit-from" type="date" className="input w-36" value={filter.from} onChange={(e) => setFilter({ ...filter, from: e.target.value })} title={__('From')} />
            <span className="text-gray-400">–</span>
            <label className="sr-only" htmlFor="audit-to">{__('To')}</label>
            <input id="audit-to" type="date" className="input w-36" value={filter.to} onChange={(e) => setFilter({ ...filter, to: e.target.value })} title={__('To')} />
            <span className="whitespace-nowrap text-gray-500">{search.results.length} {__('records')}</span>
          </div>
        }
      />
      {error && <p className="px-4 py-2 text-sm text-red-700" role="alert">{error}</p>}
      <ListView
        rows={search.results}
        columns={COLUMNS}
        rowKey={(e) => e.id}
        loading={loading}
        groupBy={search.groupBy}
        onOpen={setOpen}
        rowClassName={(e) => (e.action === LOGIN_FAILED ? 'bg-red-50/40' : '')}
      />
      {open && <AuditEntryDialog entry={open} onClose={() => setOpen(null)} />}
    </>
  );
}

function AuditEntryDialog({ entry, onClose }: { entry: AuditEntry; onClose: () => void }) {
  const group = actionGroup(entry.action);
  return (
    <Dialog title={entry.summary} onClose={onClose} size="lg">
      <dl className="grid grid-cols-[8rem_1fr] gap-x-4 gap-y-1.5 text-sm">
        <dt className="text-gray-500">{__('Date')}</dt>
        <dd className="tabular-nums">{dateTime(entry.occurredAt)}</dd>
        <dt className="text-gray-500">{__('User')}</dt>
        <dd>{entry.actor ?? NO_ACTOR}</dd>
        <dt className="text-gray-500">{__('Area')}</dt>
        <dd><Badge tone={groupTone(group)}>{groupLabel(group)}</Badge></dd>
        <dt className="text-gray-500">{__('Event')}</dt>
        <dd><code className="rounded bg-gray-100 px-1.5 py-0.5 text-xs">{entry.action}</code></dd>
        {entry.subject && (
          <>
            <dt className="text-gray-500">{__('Reference')}</dt>
            <dd className="font-medium">{entry.subject}</dd>
          </>
        )}
      </dl>
      <div>
        <h3 className="mb-1 text-sm font-semibold text-gray-700">{__('Details')}</h3>
        <pre className="max-h-96 overflow-auto rounded border border-gray-200 bg-gray-50 p-3 text-xs leading-relaxed">{JSON.stringify(entry.payload, null, 2)}</pre>
      </div>
    </Dialog>
  );
}
