import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { login } from '../service/auth';
import ErrorMessage from '../components/ErrorMessage';

const Login = ({ login: authLogin }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('candidate');
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    if (!email || !password) {
      setError('Please enter both email and password.');
      setLoading(false);
      return;
    }

    try {
      const token = await login(email, password, role);
      console.log('Login successful - Token:', token);
      authLogin(token, role);

      switch (role.toLowerCase()) {
        case 'recruiter':
          navigate('/recruiter/welcome');
          break;
        case 'candidate':
          navigate('/candidate/welcome');
          break;
        case 'admin':
          navigate('/admin/dashboard');
          break;
        default:
          setError('Unknown role selected.');
          break;
      }
    } catch (err) {
      setError(err.message || err.response?.data?.error || 'Login failed. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <h2>Login to HireSphere</h2>
      <ErrorMessage message={error} />
      {loading && <p className="loading">Logging in...</p>}
      <form onSubmit={handleSubmit} className="auth-form">
        <div className="form-group">
          <label>Role</label>
          <select value={role} onChange={(e) => setRole(e.target.value)} disabled={loading}>
            <option value="candidate">Candidate</option>
            <option value="recruiter">Recruiter</option>
          </select>
        </div>
        <div className="form-group">
          <label>Email <span className="required">*</span></label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="Enter your email"
            required
            disabled={loading}
          />
        </div>
        <div className="form-group">
          <label>Password <span className="required">*</span></label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="Enter your password"
            required
            disabled={loading}
          />
        </div>
        <button type="submit" className="btn" disabled={loading}>
          {loading ? 'Logging in...' : 'Login'}
        </button>
      </form>
      <p>
        Don’t have an account? <Link to="/signup">Register here</Link>
      </p>
    </div>
  );
};

export default Login;