import { ChevronDown } from 'lucide-react';
import { useEffect, useId, useRef, useState, type KeyboardEvent } from 'react';
import { __ } from '../lib/i18n';

export interface Option {
  value: number;
  label: string;
  /** Secondary text shown on the right of the option, e.g. stock on hand. */
  hint?: string;
}

interface Many2OneProps {
  options: readonly Option[];
  value: number | null;
  onChange: (value: number | null) => void;
  placeholder?: string;
  required?: boolean;
  disabled?: boolean;
  id?: string;
  'aria-label'?: string;
  /** Boxed look for dialogs and filters; underlined look inside form sheets. */
  variant?: 'sheet' | 'boxed';
  autoFocus?: boolean;
}

/** A searchable select in the spirit of Odoo's many2one field: type to filter, arrows and Enter to pick. */
export function Many2One({ options, value, onChange, placeholder = __('Search…'), required, disabled, id, variant = 'sheet', autoFocus, ...rest }: Many2OneProps) {
  const listId = useId();
  const selected = options.find((o) => o.value === value) ?? null;
  const [query, setQuery] = useState(selected?.label ?? '');
  const [open, setOpen] = useState(false);
  const [active, setActive] = useState(0);
  const root = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!open) setQuery(selected?.label ?? '');
  }, [selected, open]);

  useEffect(() => {
    const close = (e: MouseEvent) => {
      if (root.current && !root.current.contains(e.target as Node)) setOpen(false);
    };
    document.addEventListener('mousedown', close);
    return () => document.removeEventListener('mousedown', close);
  }, []);

  const needle = query.trim().toLowerCase();
  const filtered = (open && selected?.label === query ? options : options.filter((o) => o.label.toLowerCase().includes(needle))).slice(0, 50);

  const pick = (option: Option) => {
    onChange(option.value);
    setQuery(option.label);
    setOpen(false);
  };

  const onKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setOpen(true);
      setActive((i) => Math.min(i + 1, filtered.length - 1));
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setActive((i) => Math.max(i - 1, 0));
    } else if (e.key === 'Enter' && open) {
      e.preventDefault();
      if (filtered[active]) pick(filtered[active]);
    } else if (e.key === 'Escape') {
      setOpen(false);
    }
  };

  return (
    <div ref={root} className="relative">
      <input
        id={id}
        aria-label={rest['aria-label']}
        role="combobox"
        aria-expanded={open}
        aria-controls={listId}
        aria-autocomplete="list"
        className={`${variant === 'sheet' ? 'o-input' : 'input'} pr-6`}
        value={query}
        placeholder={placeholder}
        required={required}
        disabled={disabled}
        autoFocus={autoFocus}
        autoComplete="off"
        onFocus={(e) => {
          e.target.select();
          setOpen(true);
          setActive(0);
        }}
        onChange={(e) => {
          setQuery(e.target.value);
          setOpen(true);
          setActive(0);
          if (e.target.value === '') onChange(null);
        }}
        onBlur={() => {
          // Typed text that matches nothing is not a selection; restore the chosen label.
          window.setTimeout(() => setQuery(selected?.label ?? ''), 150);
        }}
        onKeyDown={onKeyDown}
      />
      {!disabled && <ChevronDown className="pointer-events-none absolute top-1/2 right-1 h-4 w-4 -translate-y-1/2 text-gray-400" />}
      {open && !disabled && (
        <ul id={listId} role="listbox" className="absolute z-30 mt-1 max-h-64 w-full min-w-56 overflow-y-auto rounded border border-gray-200 bg-white py-1 shadow-lg">
          {filtered.length === 0 && <li className="px-3 py-1.5 text-gray-500 italic">{__('No records found')}</li>}
          {filtered.map((o, i) => (
            <li
              key={o.value}
              role="option"
              aria-selected={o.value === value}
              className={`flex cursor-pointer items-center justify-between gap-3 px-3 py-1.5 ${i === active ? 'bg-brand-light' : ''} ${o.value === value ? 'font-medium' : ''}`}
              onMouseEnter={() => setActive(i)}
              onMouseDown={(e) => {
                e.preventDefault();
                pick(o);
              }}
            >
              <span className="truncate">{o.label}</span>
              {o.hint && <span className="shrink-0 text-xs text-gray-500">{o.hint}</span>}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
