import { CircleAlert, CircleCheck, X } from 'lucide-react';
import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react';
import { ApiError } from '../lib/http';
import { __ } from '../lib/i18n';

type Tone = 'success' | 'danger';

interface Toast {
  id: number;
  tone: Tone;
  message: string;
}

interface ToastApi {
  success: (message: string) => void;
  error: (message: string) => void;
  /** Runs an action, toasting the API error message on failure. Resolves to whether it succeeded. */
  run: (action: () => Promise<unknown>, successMessage?: string) => Promise<boolean>;
}

const ToastContext = createContext<ToastApi | null>(null);

const TONE: Record<Tone, { icon: ReactNode; className: string }> = {
  success: { icon: <CircleCheck className="h-5 w-5 text-emerald-600" />, className: 'border-l-emerald-500' },
  danger: { icon: <CircleAlert className="h-5 w-5 text-red-600" />, className: 'border-l-red-500' },
};

let nextId = 1;

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([]);

  const dismiss = useCallback((id: number) => setToasts((all) => all.filter((t) => t.id !== id)), []);

  const push = useCallback(
    (tone: Tone, message: string) => {
      const id = nextId++;
      setToasts((all) => [...all, { id, tone, message }]);
      window.setTimeout(() => dismiss(id), tone === 'danger' ? 7000 : 3500);
    },
    [dismiss],
  );

  const api = useMemo<ToastApi>(() => {
    const success = (message: string) => push('success', message);
    const error = (message: string) => push('danger', message);
    return {
      success,
      error,
      run: async (action, successMessage) => {
        try {
          await action();
          if (successMessage !== undefined) success(successMessage);
          return true;
        } catch (e) {
          error(e instanceof ApiError ? e.message : __('Something went wrong.'));
          return false;
        }
      },
    };
  }, [push]);

  return (
    <ToastContext.Provider value={api}>
      {children}
      <div className="no-print pointer-events-none fixed top-14 right-4 z-50 flex w-80 flex-col gap-2" aria-live="polite">
        {toasts.map((t) => (
          <div key={t.id} role={t.tone === 'danger' ? 'alert' : 'status'} className={`pointer-events-auto flex items-start gap-2 rounded border border-l-4 border-gray-200 bg-white p-3 shadow-lg ${TONE[t.tone].className}`}>
            {TONE[t.tone].icon}
            <p className="flex-1 text-sm">{t.message}</p>
            <button className="text-gray-400 hover:text-gray-700" onClick={() => dismiss(t.id)} aria-label={__('Dismiss')}>
              <X className="h-4 w-4" />
            </button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast(): ToastApi {
  const context = useContext(ToastContext);
  if (context === null) throw new Error('useToast must be used inside ToastProvider');
  return context;
}
