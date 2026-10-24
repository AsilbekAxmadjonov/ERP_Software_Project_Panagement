import { useState, type FormEvent } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { ApiError } from '../../../lib/http';
import { __ } from '../../../lib/i18n';
import { route } from '../../../lib/routes';
import { useSession } from '../session';

export function LoginPage() {
  const { user, login } = useSession();
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [processing, setProcessing] = useState(false);

  if (user !== null) return <Navigate to={route('home')} replace />;

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setProcessing(true);
    setError(null);
    try {
      await login(username, password);
      navigate(route('home'), { replace: true });
    } catch (e) {
      setError(e instanceof ApiError ? e.message : __('Could not reach the server.'));
    } finally {
      setProcessing(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-[#f6f1f5] via-white to-[#e6f3f3] p-6">
      <div className="w-full max-w-sm">
        <div className="mb-6 flex flex-col items-center gap-2">
          <span className="flex h-14 w-14 items-center justify-center rounded-2xl bg-brand text-2xl font-bold text-white shadow-md" aria-hidden="true">K</span>
          <span className="text-2xl font-semibold text-gray-800">{__('Kaizen ERP')}</span>
        </div>
        <form onSubmit={submit} className="space-y-4 rounded-md border border-gray-200 bg-white p-6 shadow-md">
          <div>
            <label className="label" htmlFor="username">{__('Login')}</label>
            <input id="username" className="input" value={username} onChange={(e) => setUsername(e.target.value)} autoFocus autoComplete="username" required />
          </div>
          <div>
            <label className="label" htmlFor="password">{__('Password')}</label>
            <input id="password" type="password" className="input" value={password} onChange={(e) => setPassword(e.target.value)} autoComplete="current-password" required />
          </div>
          {error !== null && <p role="alert" className="rounded border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>}
          <button className="btn w-full py-2" disabled={processing}>{processing ? __('Logging in…') : __('Log in')}</button>
        </form>
        <p className="mt-4 text-center text-xs text-gray-500">{__('Powered by Team Kaizen')}</p>
      </div>
    </div>
  );
}
