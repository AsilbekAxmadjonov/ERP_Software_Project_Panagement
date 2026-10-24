import { ChevronDown, ChevronRight, ChevronUp } from 'lucide-react';
import { Fragment, useState, type ReactNode } from 'react';
import { __ } from '../lib/i18n';
import type { GroupByDef } from './search';

export interface Column<T> {
  key: string;
  label: string;
  render: (row: T) => ReactNode;
  /** Enables sorting on this column. */
  sortValue?: (row: T) => string | number;
  align?: 'left' | 'right';
  /** Adds a total for this column in the footer and in group headers. */
  sum?: (row: T) => number;
  formatSum?: (value: number) => string;
  className?: string;
  /** Plain value for Excel export when render returns markup without a usable sortValue (badges, styled spans). */
  exportValue?: (row: T) => string | number | null;
  /** How exported numbers are shown; right-aligned columns default to money. */
  exportFormat?: 'money' | 'quantity' | 'text';
}

interface ListViewProps<T> {
  rows: readonly T[];
  columns: readonly Column<T>[];
  rowKey: (row: T) => string | number;
  onOpen?: (row: T) => void;
  groupBy?: GroupByDef<T> | null;
  rowClassName?: (row: T) => string;
  loading?: boolean;
  empty?: ReactNode;
}

type Sort = { key: string; dir: 1 | -1 } | null;

export function ListView<T>({ rows, columns, rowKey, onOpen, groupBy, rowClassName, loading, empty }: ListViewProps<T>) {
  const [sort, setSort] = useState<Sort>(null);
  const [expanded, setExpanded] = useState<ReadonlySet<string>>(new Set());

  const sortColumn = sort && columns.find((c) => c.key === sort.key);
  const sorted = sortColumn?.sortValue
    ? [...rows].sort((a, b) => {
        const x = sortColumn.sortValue!(a);
        const y = sortColumn.sortValue!(b);
        return (x < y ? -1 : x > y ? 1 : 0) * sort!.dir;
      })
    : rows;

  const hasSums = columns.some((c) => c.sum);
  const sums = (subset: readonly T[]) => columns.map((c) => (c.sum ? subset.reduce((s, r) => s + c.sum!(r), 0) : null));

  const toggleSort = (column: Column<T>) => {
    if (!column.sortValue) return;
    setSort((s) => (s?.key !== column.key ? { key: column.key, dir: 1 } : s.dir === 1 ? { key: column.key, dir: -1 } : null));
  };

  const groups = groupBy
    ? [...sorted.reduce((map, row) => map.set(groupBy.value(row), [...(map.get(groupBy.value(row)) ?? []), row]), new Map<string, T[]>())]
    : null;

  if (!loading && rows.length === 0 && empty) return <>{empty}</>;

  const renderRow = (row: T) => (
    <tr
      key={rowKey(row)}
      onClick={onOpen ? () => onOpen(row) : undefined}
      className={`border-t border-gray-100 ${onOpen ? 'cursor-pointer hover:bg-gray-50' : ''} ${rowClassName?.(row) ?? ''}`}
    >
      {columns.map((c) => (
        <td key={c.key} className={`td ${c.align === 'right' ? 'text-right tabular-nums' : ''} ${c.className ?? ''}`}>{c.render(row)}</td>
      ))}
    </tr>
  );

  return (
    <div className="overflow-x-auto bg-white">
      <table className="min-w-full">
        <thead className="border-b border-gray-300">
          <tr>
            {columns.map((c) => (
              <th
                key={c.key}
                className={`th ${c.align === 'right' ? 'text-right' : ''} ${c.sortValue ? 'cursor-pointer select-none hover:bg-gray-50' : ''}`}
                onClick={() => toggleSort(c)}
                aria-sort={sort?.key === c.key ? (sort.dir === 1 ? 'ascending' : 'descending') : undefined}
              >
                <span className={`inline-flex items-center gap-1 ${c.align === 'right' ? 'flex-row-reverse' : ''}`}>
                  {c.label}
                  {sort?.key === c.key && (sort.dir === 1 ? <ChevronUp className="h-3.5 w-3.5" /> : <ChevronDown className="h-3.5 w-3.5" />)}
                </span>
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {loading && rows.length === 0 && (
            <tr><td colSpan={columns.length} className="td py-6 text-center text-gray-500">{__('Loading…')}</td></tr>
          )}
          {!loading && rows.length === 0 && (
            <tr><td colSpan={columns.length} className="td py-6 text-center text-gray-500">{__('No records found')}</td></tr>
          )}
          {groups
            ? groups.map(([label, members]) => {
                const isOpen = expanded.has(label);
                const groupSums = sums(members);
                return (
                  <Fragment key={label}>
                    <tr
                      className="cursor-pointer border-t border-gray-200 bg-gray-50 font-medium hover:bg-gray-100"
                      onClick={() => setExpanded((set) => {
                        const next = new Set(set);
                        if (isOpen) next.delete(label);
                        else next.add(label);
                        return next;
                      })}
                    >
                      {columns.map((c, i) => (
                        <td key={c.key} className={`td ${c.align === 'right' ? 'text-right tabular-nums' : ''}`}>
                          {i === 0 ? (
                            <span className="inline-flex items-center gap-1">
                              {isOpen ? <ChevronDown className="h-4 w-4" /> : <ChevronRight className="h-4 w-4" />}
                              {label || __('None')} ({members.length})
                            </span>
                          ) : groupSums[i] !== null ? (c.formatSum ?? String)(groupSums[i]!) : null}
                        </td>
                      ))}
                    </tr>
                    {isOpen && members.map(renderRow)}
                  </Fragment>
                );
              })
            : sorted.map(renderRow)}
        </tbody>
        {hasSums && rows.length > 0 && (
          <tfoot className="border-t-2 border-gray-300 font-semibold">
            <tr>
              {sums(rows).map((value, i) => (
                <td key={columns[i].key} className={`td ${columns[i].align === 'right' ? 'text-right tabular-nums' : ''}`}>
                  {value !== null ? (columns[i].formatSum ?? String)(value) : null}
                </td>
              ))}
            </tr>
          </tfoot>
        )}
      </table>
    </div>
  );
}
