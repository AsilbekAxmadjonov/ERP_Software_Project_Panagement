import { ChevronRight, Filter, Kanban, Layers, List, Search, X } from 'lucide-react';
import { Fragment, useEffect, useRef, useState, type ReactNode } from 'react';
import { Link } from 'react-router-dom';
import { __ } from '../lib/i18n';
import type { SearchState } from './search';

export interface Crumb {
  label: string;
  to?: string;
}

export type ViewMode = 'list' | 'kanban';

interface ControlPanelProps<T> {
  breadcrumbs: readonly Crumb[];
  /** Buttons left of the breadcrumbs, usually "New". */
  actions?: ReactNode;
  search?: SearchState<T>;
  view?: { mode: ViewMode; onChange: (mode: ViewMode) => void };
  /** Shown on the right, e.g. a record count or a pager. */
  aside?: ReactNode;
}

export function ControlPanel<T>({ breadcrumbs, actions, search, view, aside }: ControlPanelProps<T>) {
  return (
    <div className="no-print border-b border-gray-200 bg-white px-4 py-2.5">
      <div className="flex flex-wrap items-center gap-x-4 gap-y-2">
        <div className="flex min-w-0 flex-1 items-center gap-3">
          {actions}
          <Breadcrumbs items={breadcrumbs} />
        </div>
        {search && <SearchBar search={search} />}
        <div className="flex flex-1 items-center justify-end gap-3">
          {aside ?? (search && <span className="text-sm text-gray-500">{search.results.length} {__('records')}</span>)}
          {view && <ViewSwitcher {...view} />}
        </div>
      </div>
    </div>
  );
}

function Breadcrumbs({ items }: { items: readonly Crumb[] }) {
  return (
    <nav aria-label={__('Breadcrumb')} className="flex min-w-0 items-center gap-1 text-lg">
      {items.map((c, i) => (
        <Fragment key={i}>
          {i > 0 && <ChevronRight className="h-4 w-4 shrink-0 text-gray-400" />}
          {c.to && i < items.length - 1 ? (
            <Link to={c.to} className="truncate text-accent hover:underline">{c.label}</Link>
          ) : (
            <span className="truncate text-gray-800">{c.label}</span>
          )}
        </Fragment>
      ))}
    </nav>
  );
}

function SearchBar<T>({ search }: { search: SearchState<T> }) {
  const [open, setOpen] = useState(false);
  const root = useRef<HTMLDivElement>(null);
  const activeFilters = search.filters.filter((f) => search.active.includes(f.key));
  const hasOptions = search.filters.length > 0 || search.groupBys.length > 0;

  useEffect(() => {
    const close = (e: MouseEvent) => {
      if (root.current && !root.current.contains(e.target as Node)) setOpen(false);
    };
    document.addEventListener('mousedown', close);
    return () => document.removeEventListener('mousedown', close);
  }, []);

  return (
    <div ref={root} className="relative order-last w-full md:order-none md:w-[28rem]">
      <div className="flex items-center rounded border border-gray-300 bg-white focus-within:border-accent focus-within:ring-1 focus-within:ring-accent">
        <Search className="ml-2 h-4 w-4 shrink-0 text-gray-400" />
        <div className="flex min-w-0 flex-1 flex-wrap items-center gap-1 px-1.5 py-1">
          {activeFilters.length > 0 && (
            <Facet icon={<Filter className="h-3 w-3" />} onRemove={() => activeFilters.forEach((f) => search.toggle(f.key))}>
              {activeFilters.map((f) => f.label).join(__(' or '))}
            </Facet>
          )}
          {search.groupBy && (
            <Facet icon={<Layers className="h-3 w-3" />} onRemove={() => search.setGroupBy(null)}>{search.groupBy.label}</Facet>
          )}
          <input
            aria-label={__('Search')}
            className="min-w-24 flex-1 border-0 bg-transparent py-0.5 text-sm focus:ring-0 focus:outline-none"
            placeholder={__('Search…')}
            value={search.query}
            onChange={(e) => search.setQuery(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Backspace' && search.query === '') {
                if (search.groupBy) search.setGroupBy(null);
                else activeFilters.forEach((f) => search.toggle(f.key));
              }
            }}
          />
        </div>
        {hasOptions && (
          <button type="button" className="border-l border-gray-300 px-2 py-1.5 text-gray-500 hover:bg-gray-50" onClick={() => setOpen(!open)} aria-expanded={open} aria-label={__('Toggle search panel')}>
            <Filter className="h-4 w-4" />
          </button>
        )}
      </div>
      {open && (
        <div className="absolute right-0 z-30 mt-1 grid w-full min-w-80 grid-cols-2 gap-4 rounded border border-gray-200 bg-white p-3 shadow-lg">
          <div>
            <h3 className="mb-1 flex items-center gap-1.5 font-semibold text-gray-700"><Filter className="h-4 w-4 text-brand" />{__('Filters')}</h3>
            {search.filters.map((f, i) => (
              <Fragment key={f.key}>
                {i > 0 && (f.group ?? f.key) !== (search.filters[i - 1].group ?? search.filters[i - 1].key) && <hr className="my-1 border-gray-200" />}
                <OptionButton active={search.active.includes(f.key)} onClick={() => search.toggle(f.key)}>{f.label}</OptionButton>
              </Fragment>
            ))}
          </div>
          {search.groupBys.length > 0 && (
            <div>
              <h3 className="mb-1 flex items-center gap-1.5 font-semibold text-gray-700"><Layers className="h-4 w-4 text-accent" />{__('Group By')}</h3>
              {search.groupBys.map((g) => (
                <OptionButton key={g.key} active={search.groupBy?.key === g.key} onClick={() => search.setGroupBy(search.groupBy?.key === g.key ? null : g.key)}>
                  {g.label}
                </OptionButton>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}

function Facet({ icon, children, onRemove }: { icon: ReactNode; children: ReactNode; onRemove: () => void }) {
  return (
    <span className="inline-flex max-w-full items-center gap-1 rounded bg-gray-100 text-xs">
      <span className="rounded-l bg-brand px-1.5 py-1 text-white">{icon}</span>
      <span className="truncate py-0.5">{children}</span>
      <button type="button" onClick={onRemove} className="px-1 text-gray-500 hover:text-gray-800" aria-label={__('Remove')}>
        <X className="h-3 w-3" />
      </button>
    </span>
  );
}

function OptionButton({ active, onClick, children }: { active: boolean; onClick: () => void; children: ReactNode }) {
  return (
    <button type="button" onClick={onClick} className={`flex w-full items-center gap-2 rounded px-2 py-1 text-left hover:bg-gray-100 ${active ? 'font-medium text-gray-900' : 'text-gray-600'}`}>
      <span className={`h-1.5 w-1.5 rounded-full ${active ? 'bg-accent' : 'bg-transparent'}`} />
      {children}
    </button>
  );
}

const VIEW_ICON: Record<ViewMode, { icon: ReactNode; label: string }> = {
  list: { icon: <List className="h-4 w-4" />, label: __('List') },
  kanban: { icon: <Kanban className="h-4 w-4" />, label: __('Kanban') },
};

function ViewSwitcher({ mode, onChange }: { mode: ViewMode; onChange: (mode: ViewMode) => void }) {
  return (
    <div className="flex overflow-hidden rounded border border-gray-300">
      {(Object.keys(VIEW_ICON) as ViewMode[]).map((m) => (
        <button
          key={m}
          type="button"
          title={VIEW_ICON[m].label}
          aria-label={VIEW_ICON[m].label}
          aria-pressed={mode === m}
          onClick={() => onChange(m)}
          className={`px-2 py-1.5 ${mode === m ? 'bg-gray-200 text-gray-900' : 'bg-white text-gray-500 hover:bg-gray-50'}`}
        >
          {VIEW_ICON[m].icon}
        </button>
      ))}
    </div>
  );
}
