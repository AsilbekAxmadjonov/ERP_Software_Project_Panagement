import type { ReactNode } from 'react';
import { __ } from '../lib/i18n';
import { initials } from '../lib/format';

export function KanbanGrid<T>({ rows, rowKey, renderCard, onOpen }: { rows: readonly T[]; rowKey: (row: T) => string | number; renderCard: (row: T) => ReactNode; onOpen?: (row: T) => void }) {
  if (rows.length === 0) return <p className="p-8 text-center text-gray-500">{__('No records found')}</p>;
  return (
    <div className="grid gap-2 p-3 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
      {rows.map((row) => (
        <div
          key={rowKey(row)}
          role={onOpen ? 'button' : undefined}
          tabIndex={onOpen ? 0 : undefined}
          onClick={onOpen ? () => onOpen(row) : undefined}
          onKeyDown={onOpen ? (e) => e.key === 'Enter' && onOpen(row) : undefined}
          className={`rounded border border-gray-200 bg-white p-3 shadow-sm ${onOpen ? 'cursor-pointer transition hover:border-gray-300 hover:shadow-md' : ''}`}
        >
          {renderCard(row)}
        </div>
      ))}
    </div>
  );
}

const AVATAR_COLORS = ['bg-rose-500', 'bg-amber-500', 'bg-emerald-600', 'bg-sky-600', 'bg-indigo-500', 'bg-fuchsia-600', 'bg-teal-600', 'bg-orange-500'];

export function Avatar({ name, size = 'md' }: { name: string; size?: 'sm' | 'md' | 'lg' }) {
  const color = AVATAR_COLORS[[...name].reduce((h, ch) => h + ch.charCodeAt(0), 0) % AVATAR_COLORS.length];
  const dims = { sm: 'h-6 w-6 text-[10px]', md: 'h-10 w-10 text-sm', lg: 'h-16 w-16 text-xl' }[size];
  return <span className={`inline-flex shrink-0 items-center justify-center rounded ${dims} ${color} font-semibold text-white`} aria-hidden="true">{initials(name) || '?'}</span>;
}
