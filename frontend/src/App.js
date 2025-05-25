import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Route, Routes, useLocation, Navigate } from 'react-router-dom';
import Navbar from './components/shared/Navbar';
import HomePage from './pages/HomePage';
import Login from './pages/Login';
import SignUp from './pages/SignUp';
import Privacy from './pages/Privacy';
import Profile from './pages/Profile';
import CandidateWelcome from './pages/CandidateWelcome';
import RecruiterWelcome from './pages/RecruiterWelcome';
import Jobs from './pages/Jobs';
import CreateJob from './pages/CreateJob';
import MyJobs from './pages/MyJobs';
import ViewApplication from './pages/ViewApplication';
import ApplyJob from './pages/ApplyJob';
import InterviewPage from './pages/InterviewBot';
import MyApplications from './pages/MyApplications';
import api from './service/api';

function App() {
  const [auth, setAuth] = useState({
    token: null, // Start null, validate later
    role: null,
  });
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const validateToken = async () => {
      const token = localStorage.getItem('token');
      const role = localStorage.getItem('role');
      if (token && role) {
        try {
          // Validate token based on role
          const endpoint = role === 'candidate' ? '/candidates/me' : '/recruiters/me';
          await api.get(endpoint, {
            headers: { Authorization: `Bearer ${token}` },
          });
          setAuth({ token, role });
        } catch (err) {
          console.error('Token validation failed:', err);
          localStorage.removeItem('token');
          localStorage.removeItem('role');
          setAuth({ token: null, role: null });
        }
      }
      setIsLoading(false);
    };
    validateToken();
  }, []);

  const login = (token, role) => {
    localStorage.setItem('token', token);
    localStorage.setItem('role', role);
    setAuth({ token, role });
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    setAuth({ token: null, role: null });
  };

  const ProtectedRoute = ({ children, allowedRole }) => {
    if (!auth.token) {
      console.log('Not authenticated, redirecting to /login');
      return <Navigate to="/login" replace />;
    }
    if (allowedRole && auth.role !== allowedRole) {
      console.log(`Role ${auth.role} not allowed, redirecting to /`);
      return <Navigate to="/" replace />;
    }
    return children;
  };

  const ConditionalNavbar = () => {
    const location = useLocation();
    const noNavbarPaths = ['/', '/signup', '/login'];
    return !noNavbarPaths.includes(location.pathname) ? <Navbar auth={auth} logout={logout} /> : null;
  };

  if (isLoading) return <div>Loading...</div>;

  return (
    <Router>
      <ConditionalNavbar />
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route
          path="/login"
          element={auth.token ? <Navigate to={`/${auth.role}/welcome`} replace /> : <Login login={login} />}
        />
        <Route path="/signup" element={<SignUp login={login} />} />
        <Route path="/privacy" element={<Privacy />} />
        <Route
          path="/profile"
          element={<ProtectedRoute><Profile auth={auth} /></ProtectedRoute>}
        />
        <Route
          path="/candidate/welcome"
          element={<ProtectedRoute allowedRole="candidate"><CandidateWelcome auth={auth} /></ProtectedRoute>}
        />
        <Route
          path="/recruiter/welcome"
          element={<ProtectedRoute allowedRole="recruiter"><RecruiterWelcome auth={auth} /></ProtectedRoute>}
        />
        <Route
          path="/jobs"
          element={<ProtectedRoute allowedRole="candidate"><Jobs auth={auth} /></ProtectedRoute>}
        />
        <Route
          path="/create-job"
          element={<ProtectedRoute allowedRole="recruiter"><CreateJob auth={auth} /></ProtectedRoute>}
        />
        <Route
          path="/my-jobs"
          element={<ProtectedRoute allowedRole="recruiter"><MyJobs auth={auth} /></ProtectedRoute>}
        />
        <Route
          path="/jobs/:jobId/applications"
          element={<ProtectedRoute allowedRole="recruiter"><ViewApplication auth={auth} /></ProtectedRoute>}
        />
        <Route
          path="/apply/:jobId"
          element={<ProtectedRoute allowedRole="candidate"><ApplyJob auth={auth} /></ProtectedRoute>}
        />
        <Route
          path="/interview/:applicationId"
          element={<ProtectedRoute allowedRole="candidate"><InterviewPage auth={auth} /></ProtectedRoute>}
        />
        <Route
          path="/candidate/myApplications"
          element={<ProtectedRoute allowedRole="candidate"><MyApplications auth={auth} /></ProtectedRoute>}
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  );
}

export default App;