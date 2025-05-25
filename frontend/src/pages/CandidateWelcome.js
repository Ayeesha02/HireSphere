import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../service/api';
import Footer from '../components/shared/Footer';
import '../style.css'; 

const CandidateWelcome = ({ auth }) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [recommendations, setRecommendations] = useState([]);
  const [profile, setProfile] = useState(null);
  const [searchResults, setSearchResults] = useState([]);
  const [jobScores, setJobScores] = useState([]);
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState({
    profile: true,
    recommendations: true,
    scores: true,
    applications: true,
    search: false, 
  });
  const [error, setError] = useState({
    profile: null,
    recommendations: null,
    scores: null,
    applications: null,
    search: null,
  });
  const navigate = useNavigate();

  useEffect(() => {
    const fetchData = async () => {
     
      try {
        const profileRes = await api.get('/candidates/me', {
          headers: { Authorization: `Bearer ${auth.token}` },
        });
        setProfile(profileRes.data);
        setLoading((prev) => ({ ...prev, profile: false }));
      } catch (err) {
        setError((prev) => ({ ...prev, profile: 'Failed to load profile.' }));
        console.error('Error fetching profile:', err);
        setLoading((prev) => ({ ...prev, profile: false }));
      }

      
      try {
        const recRes = await api.get('/candidates/job-recommendations', {
          headers: { Authorization: `Bearer ${auth.token}` },
        });
        setRecommendations(recRes.data);
        setLoading((prev) => ({ ...prev, recommendations: false }));
      } catch (err) {
        setError((prev) => ({ ...prev, recommendations: 'Failed to load recommendations.' }));
        console.error('Error fetching recommendations:', err);
        setLoading((prev) => ({ ...prev, recommendations: false }));
      }

      
      try {
        const scoresRes = await api.get('/candidates/job-scores', {
          headers: { Authorization: `Bearer ${auth.token}` },
        });
        setJobScores(scoresRes.data);
        setLoading((prev) => ({ ...prev, scores: false }));
      } catch (err) {
        setError((prev) => ({ ...prev, scores: 'Failed to load job scores.' }));
        console.error('Error fetching job scores:', err);
        setLoading((prev) => ({ ...prev, scores: false }));
      }

     
      try {
        const appsRes = await api.get('/candidates/applications', {
          headers: { Authorization: `Bearer ${auth.token}` },
        });
        setApplications(appsRes.data.slice(0, 3)); // Limit to 3
        setLoading((prev) => ({ ...prev, applications: false }));
      } catch (err) {
        setError((prev) => ({ ...prev, applications: 'Failed to load applications.' }));
        console.error('Error fetching applications:', err);
        setLoading((prev) => ({ ...prev, applications: false }));
      }
    };
    fetchData();
  }, [auth.token]);

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!searchQuery.trim()) return;

    setLoading((prev) => ({ ...prev, search: true }));
    setError((prev) => ({ ...prev, search: null }));
    try {
      const res = await api.get('/candidates/jobs/search', {
        params: { keyword: searchQuery },
        headers: { Authorization: `Bearer ${auth.token}` },
      });
      setSearchResults(res.data);
    } catch (err) {
      setError((prev) => ({ ...prev, search: 'Search failed. Please try again.' }));
      console.error('Search error:', err);
    } finally {
      setLoading((prev) => ({ ...prev, search: false }));
    }
  };

  const handleApply = (jobId) => {
    navigate(`/apply/${jobId}`);
  };

  return (
    <div className="welcome-container">
      
      <section className="welcome-section">
        <div className="welcome-header">
      <h2>Welcome, {profile?.name || 'Candidate'}!</h2>
      <p>Find your dream job with HireSphere.</p>
        </div>

    
      {Object.values(error).some((e) => e) && (
        <div className="error-messages">
            {Object.entries(error).map(([key, value]) => 
              value && <p key={key} className="error">{value}</p>
            )}
        </div>
      )}

     
      <form onSubmit={handleSearch} className="search-form">
        <div className="search-wrapper">
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search by job title, skills, or location..."
            className="search-input"
            disabled={loading.search}
          />
          <button type="submit" className="search-btn" disabled={loading.search}>
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="24"
              height="24"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <circle cx="11" cy="11" r="8" />
              <line x1="21" y1="21" x2="16.65" y2="16.65" />
            </svg>
          </button>
        </div>
      </form>

        
        <div className="quick-links">
          <Link to="/profile" className="action-link">Edit Profile</Link>
          <Link to="/candidate/myApplications" className="action-link">My Applications</Link>
          <Link to="/jobs" className="action-link">Browse All Jobs</Link>
        </div>

     
      {loading.search && <p className="loading">Searching...</p>}
      {searchResults.length > 0 && (
        <div className="search-results">
          <h3>Search Results</h3>
          <ul>
            {searchResults.map((job) => (
              <li key={job.id} className="job-item">
                <Link to={`/jobs/${job.id}`}>{job.title} - {job.location || 'N/A'}</Link>
                <button onClick={() => handleApply(job.id)} className="apply-btn" disabled={loading.search}>
                  Apply
                </button>
              </li>
            ))}
          </ul>
        </div>
      )}
      </section>

     
      <section className="recommendations-section">
        <h3>Job Recommendations</h3>
        {loading.recommendations || loading.scores ? (
          <p className="loading">Loading recommendations...</p>
        ) : recommendations.length > 0 ? (
          <ul className="recommendations-list">
            {recommendations
              .filter(job => {
                const score = jobScores.find((s) => s.job.id === job.id)?.matchScore || 0;
                return score > 40;
              })
              .map((job) => {
              const score = jobScores.find((s) => s.job.id === job.id)?.matchScore || 'N/A';
              return (
                <li key={job.id} className="job-item">
                    <div className="job-info">
                      <Link to={`/jobs/${job.id}`} className="job-title">{job.title}</Link>
                      <span className="job-location">{job.location || 'N/A'}</span>
                    </div>
                    <div className="job-actions">
                      <span className="match-score">Match: {score}%</span>
                  <button onClick={() => handleApply(job.id)} className="apply-btn">
                    Apply
                  </button>
                    </div>
                </li>
              );
            })}
          </ul>
        ) : (
          <p className="no-recommendations">No recommendations yet. Update your profile for better matches!</p>
        )}

      
      <div className="recent-applications">
        <h3>Recent Applications</h3>
        {loading.applications ? (
          <p className="loading">Loading applications...</p>
        ) : applications.length > 0 ? (
            <ul className="applications-list">
              {applications.map((app) => (
                <li key={app.id} className="application-item">
                  <Link to={`/interview/${app.id}`} className="application-link">
                    <span className="application-title">{app.job.title}</span>
                    <span className={`application-status ${app.status?.toLowerCase()}`}>
                      Status: {app.status || 'Pending'}
                    </span>
                  </Link>
                </li>
              ))}
            </ul>
        ) : (
            <p className="no-applications">No recent applications.</p>
        )}
      </div>
      </section>
      <Footer />
    </div>
  );
};

export default CandidateWelcome;