import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { ArrowUpRight } from 'lucide-react';
import { authService } from '../services/authService';
import { useAuthStore } from '../store/authStore';

export function Auth({
  setUser,
  register = false
}: {
  setUser: (x: any) => void;
  register?: boolean;
}) {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errorMsg, setErrorMsg] = useState('');
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const nextTarget = searchParams.get('next') || '/account';

  const storeLogin = useAuthStore((state) => state.login);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg('');
    setLoading(true);

    try {
      if (register) {
        await authService.signUp({ name, email, password });
        navigate(`/login?next=${encodeURIComponent(nextTarget)}`);
        return;
      }

      const u = await authService.signIn({ email, password });
      storeLogin(u);
      setUser(u);
      navigate(nextTarget);
    } catch (err) {
      setErrorMsg(
        err instanceof Error
          ? err.message
          : 'Authentication failed. Please verify your credentials or ensure the backend is reachable.'
      );
    } finally {
      setLoading(false);
    }
  };

  const switchLink = register
    ? `/login${nextTarget !== '/account' ? `?next=${encodeURIComponent(nextTarget)}` : ''}`
    : `/register${nextTarget !== '/account' ? `?next=${encodeURIComponent(nextTarget)}` : ''}`;

  return (
    <div className="page auth-page">
      <div className="auth-art">
        <div className="auth-art-image" />
        <div>
          <span className="eyebrow">THE NOVA EXPERIENCE</span>
          <h2>
            Good things<br />are worth <em>finding.</em>
          </h2>
          <p>A more personal way to discover the objects you’ll love.</p>
        </div>
        <span className="auth-art-caption">N / STUDIO 2026</span>
      </div>

      <div className="auth-form-wrap">
        <span className="eyebrow">{register ? 'CREATE YOUR ACCOUNT' : 'WELCOME BACK'}</span>
        <h1>{register ? 'A place of your own.' : 'Good to see you.'}</h1>
        <p>
          {register
            ? 'Create an account to save your discoveries and streamline checkout.'
            : 'Sign in to continue your NOVA experience and manage orders.'}
        </p>

        {errorMsg && <p className="error-message">{errorMsg}</p>}

        <form onSubmit={submit}>
          {register && (
            <label className="form-label">
              Full name
              <input
                required
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Your full name"
              />
            </label>
          )}

          <label className="form-label">
            Email address
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="you@example.com"
            />
          </label>

          <label className="form-label">
            Password
            <input
              type="password"
              required
              minLength={8}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="At least 8 characters"
            />
          </label>

          <button className="button button-gold full" disabled={loading}>
            {loading ? 'Authenticating…' : register ? 'Create account' : 'Sign in'}{' '}
            <ArrowUpRight size={15} />
          </button>
        </form>

        <p className="auth-switch">
          {register ? 'Already have an account?' : 'New to NOVA?'}{' '}
          <Link to={switchLink}>{register ? 'Sign in' : 'Create an account'}</Link>
        </p>

        <small className="muted">
          Authentication is handled by the Spring Boot API using BCrypt password hashing and JWT bearer tokens.
        </small>
      </div>
    </div>
  );
}
