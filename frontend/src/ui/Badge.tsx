import type { ReactNode } from 'react';

export type Tone = 'gray' | 'blue' | 'green' | 'yellow' | 'red' | 'purple';

const TONE: Record<Tone, string> = {
  gray: 'bg-gray-100 text-gray-700',
  blue: 'bg-sky-100 text-sky-800',
  green: 'bg-emerald-100 text-emerald-800',
  yellow: 'bg-amber-100 text-amber-800',
  red: 'bg-red-100 text-red-800',
  purple: 'bg-brand-light text-brand',
};

export function Badge({ tone, children }: { tone: Tone; children: ReactNode }) {
  return <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium whitespace-nowrap ${TONE[tone]}`}>{children}</span>;
}

const RIBBON: Record<Tone, string> = {
  gray: 'bg-gray-500',
  blue: 'bg-sky-600',
  green: 'bg-emerald-600',
  yellow: 'bg-amber-500',
  red: 'bg-red-600',
  purple: 'bg-brand',
};

/** The diagonal corner ribbon Odoo puts on paid / cancelled records; sits below the smart-button strip. */
export function Ribbon({ tone, children }: { tone: Tone; children: ReactNode }) {
  return (
    <div className="pointer-events-none absolute top-14 right-0 h-28 w-28 overflow-hidden">
      <div className={`absolute top-6 -right-8 w-40 rotate-45 py-1 text-center text-xs font-bold tracking-wider text-white uppercase shadow ${RIBBON[tone]}`}>
        {children}
      </div>
    </div>
  );
}
