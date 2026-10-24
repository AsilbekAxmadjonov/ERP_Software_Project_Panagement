import { useState } from 'react';

export interface FilterDef<T> {
  key: string;
  label: string;
  /** Filters in the same group are OR-ed together; different groups are AND-ed (Odoo semantics). */
  group?: string;
  test: (row: T) => boolean;
}

export interface GroupByDef<T> {
  key: string;
  label: string;
  value: (row: T) => string;
}

export interface SearchConfig<T> {
  /** Text the search box matches against. */
  text: (row: T) => readonly (string | null | undefined)[];
  filters?: readonly FilterDef<T>[];
  groupBys?: readonly GroupByDef<T>[];
  defaultFilters?: readonly string[];
}

export interface SearchState<T> {
  query: string;
  setQuery: (query: string) => void;
  filters: readonly FilterDef<T>[];
  groupBys: readonly GroupByDef<T>[];
  active: readonly string[];
  toggle: (key: string) => void;
  groupBy: GroupByDef<T> | null;
  setGroupBy: (key: string | null) => void;
  clear: () => void;
  results: T[];
}

export function useSearch<T>(rows: readonly T[], config: SearchConfig<T>): SearchState<T> {
  const [query, setQuery] = useState('');
  const [active, setActive] = useState<readonly string[]>(config.defaultFilters ?? []);
  const [groupKey, setGroupKey] = useState<string | null>(null);
  const filters = config.filters ?? [];
  const groupBys = config.groupBys ?? [];

  const selected = filters.filter((f) => active.includes(f.key));
  const byGroup = new Map<string, FilterDef<T>[]>();
  for (const f of selected) byGroup.set(f.group ?? f.key, [...(byGroup.get(f.group ?? f.key) ?? []), f]);

  const needle = query.trim().toLowerCase();
  const results = rows.filter(
    (row) =>
      (needle === '' || config.text(row).some((v) => v != null && v.toLowerCase().includes(needle))) &&
      [...byGroup.values()].every((group) => group.some((f) => f.test(row))),
  );

  return {
    query,
    setQuery,
    filters,
    groupBys,
    active,
    toggle: (key) => setActive((keys) => (keys.includes(key) ? keys.filter((k) => k !== key) : [...keys, key])),
    groupBy: groupBys.find((g) => g.key === groupKey) ?? null,
    setGroupBy: setGroupKey,
    clear: () => {
      setQuery('');
      setActive([]);
      setGroupKey(null);
    },
    results,
  };
}
