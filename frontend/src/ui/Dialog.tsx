import { X } from 'lucide-react';
import { useEffect, useId, type FormEvent, type ReactNode } from 'react';
import { __ } from '../lib/i18n';

interface DialogProps {
  title: string;
  onClose: () => void;
  /** When given, the body is a form and the primary button submits it. */
  onSubmit?: () => void | Promise<void>;
  submitLabel?: string;
  submitDisabled?: boolean;
  size?: 'md' | 'lg';
  children: ReactNode;
}

const SIZE = { md: 'max-w-lg', lg: 'max-w-3xl' };

export function Dialog({ title, onClose, onSubmit, submitLabel = __('Confirm'), submitDisabled = false, size = 'md', children }: DialogProps) {
  const titleId = useId();

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => e.key === 'Escape' && onClose();
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [onClose]);

  const submit = (event: FormEvent) => {
    event.preventDefault();
    void onSubmit?.();
  };

  return (
    <div className="no-print fixed inset-0 z-40 flex items-start justify-center overflow-y-auto bg-black/40 p-4 pt-16" onMouseDown={(e) => e.target === e.currentTarget && onClose()}>
      <form role="dialog" aria-modal="true" aria-labelledby={titleId} onSubmit={submit} className={`w-full ${SIZE[size]} rounded-md bg-white shadow-xl`}>
        <header className="flex items-center justify-between border-b border-gray-200 px-5 py-3">
          <h2 id={titleId} className="text-lg font-medium">{title}</h2>
          <button type="button" onClick={onClose} className="text-gray-400 hover:text-gray-700" aria-label={__('Close')}>
            <X className="h-5 w-5" />
          </button>
        </header>
        <div className="space-y-3 px-5 py-4">{children}</div>
        <footer className="flex gap-2 border-t border-gray-200 px-5 py-3">
          {onSubmit && <button className="btn" disabled={submitDisabled}>{submitLabel}</button>}
          <button type="button" className="btn-secondary" onClick={onClose}>{onSubmit ? __('Discard') : __('Close')}</button>
        </footer>
      </form>
    </div>
  );
}
