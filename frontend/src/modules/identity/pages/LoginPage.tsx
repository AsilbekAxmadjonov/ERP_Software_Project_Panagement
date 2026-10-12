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
    <div className="flex min-h-screen items-center justify-center p-6">
      <form onSubmit={submit} className="card w-full max-w-sm space-y-4">
        <h1 className="text-xl font-semibold">{__('Sign in to Kaizen ERP')}</h1>
        <div>
          <label className="label" htmlFor="username">{__('Username')}</label>
          <input id="username" className="input" value={username} onChange={(e) => setUsername(e.target.value)} autoFocus autoComplete="username" required />
        </div>
        <div>
          <label className="label" htmlFor="password">{__('Password')}</label>
          <input id="password" type="password" className="input" value={password} onChange={(e) => setPassword(e.target.value)} autoComplete="current-password" required />
        </div>
        {error !== null && <p role="alert" className="text-sm text-red-600">{error}</p>}
        <button className="btn w-full" disabled={processing}>{processing ? __('Signing in…') : __('Sign in')}</button>
      </form>
    </div>
  );
}
