import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../service/api';
import ErrorMessage from '../components/ErrorMessage';
import { Bar } from 'react-chartjs-2';
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend } from 'chart.js';
import Footer from '../components/shared/Footer';
import '../style.css';

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);

const RecruiterWelcome = ({ auth }) => {
  const [profile, setProfile] = useState(null);
  const [metrics, setMetrics] = useState([]);
  const [prediction, setPrediction] = useState(null);
  const [csvFile, setCsvFile] = useState(null);
  const [recentJobs, setRecentJobs] = useState([]);
  const [applicationSummary, setApplicationSummary] = useState({ newApplications: 0 });
  const [loading, setLoading] = useState({
    profile: true,
    metrics: false,
    jobs: true,
    applications: true,
    prediction: false,
  });
  const [errors, setErrors] = useState({
    profile: null,
    metrics: null,
    jobs: null,
    applications: null,
    prediction: null,
    delete: null,
  });
  const [period, setPeriod] = useState('monthly');
  const navigate = useNavigate();

  const fetchMetrics = async () => {
    setLoading(prev => ({ ...prev, metrics: true }));
    setErrors(prev => ({ ...prev, metrics: null }));
    try {
      if (!auth?.token) throw new Error('No authentication token found');
      const response = await api.post(
        `/recruiters/dashboard/generate-and-get?period=${period}`,
        null,
        { headers: { Authorization: `Bearer ${auth.token}` } }
      );
      const metricsData = Array.isArray(response.data) ? response.data : [];
      setMetrics(metricsData);
      if (metricsData.length === 0) {
        setErrors(prev => ({ ...prev, metrics: 'No metrics available for this period.' }));
      }
    } catch (err) {
      let errorMessage = 'Failed to fetch metrics. Please try again.';
      if (err.response?.status === 401) {
        errorMessage = 'Session expired. Please log in again.';
        navigate('/login');
      } else if (err.response?.status === 400) {
        errorMessage = err.response.data || 'Invalid request.';
      }
      setErrors(prev => ({ ...prev, metrics: errorMessage }));
    } finally {
      setLoading(prev => ({ ...prev, metrics: false }));
    }
  };

  useEffect(() => {
    const fetchData = async () => {
      try {
        if (!auth.token) {
          navigate('/login');
          return;
        }
        const profileRes = await api.get('/recruiters/me', {
          headers: { Authorization: `Bearer ${auth.token}` },
        });
        setProfile(profileRes.data);
        setLoading((prev) => ({ ...prev, profile: false }));

        const jobsRes = await api.get('/recruiters/jobs', {
          headers: { Authorization: `Bearer ${auth.token}` },
        });
        const jobsWithApplications = await Promise.all(
          jobsRes.data.slice(0, 5).map(async (job) => {
            const appsRes = await api.get(`/recruiters/jobs/${job.id}/applications`, {
              headers: { Authorization: `Bearer ${auth.token}` },
            });
            return { ...job, applications: appsRes.data.length };
          })
        );
        setRecentJobs(jobsWithApplications);
        setLoading((prev) => ({ ...prev, jobs: false }));

        const appsRes = await api.get('/recruiters/applications', {
          headers: { Authorization: `Bearer ${auth.token}` },
        });
        const newApps = appsRes.data.filter(
          (app) => new Date(app.applicationDate) > new Date(Date.now() - 7 * 24 * 60 * 60 * 1000)
        ).length;
        setApplicationSummary({ newApplications: newApps });
        setLoading((prev) => ({ ...prev, applications: false }));

        await fetchMetrics();
      } catch (err) {
        setErrors((prev) => ({
          ...prev,
          profile: err.response?.status === 401 ? 'Unauthorized' : 'Failed to fetch data',
        }));
        setLoading((prev) => ({ ...prev, profile: false, jobs: false, applications: false }));
        if (err.response?.status === 401) navigate('/login');
      }
    };
    fetchData();
  }, [auth.token, navigate]);

  const handleFileChange = (e) => setCsvFile(e.target.files[0]);
  
  const handlePredict = async () => {
    if (!csvFile || !(csvFile instanceof File)) {
      setErrors((prev) => ({ ...prev, prediction: 'Please upload a valid CSV file.' }));
      return;
    }
    setLoading((prev) => ({ ...prev, prediction: true }));
    setErrors((prev) => ({ ...prev, prediction: null }));
    try {
      const formData = new FormData();
      formData.append('file', csvFile);
      console.log('Sending file:', csvFile.name); // Debug
      const res = await api.post('/recruiters/predict', formData, {
        headers: {
          Authorization: `Bearer ${auth.token}`,
          // No Content-Type here—handled by api.js and Axios
        },
      });
      console.log('Prediction response:', res.data);
      setPrediction(res.data);
    } catch (err) {
      console.error('Prediction error:', err.response?.data || err.message);
      setErrors((prev) => ({
        ...prev,
        prediction: err.response?.data?.error || 'Failed to generate prediction.',
      }));
    } finally {
      setLoading((prev) => ({ ...prev, prediction: false }));
    }
  };

  const handlePeriodChange = (e) => {
    setPeriod(e.target.value);
    fetchMetrics();
  };

  const chartData = prediction
    ? {
        labels: ['Predicted Hires', 'Predicted Turnover'],
        datasets: [
          {
            label: 'Workforce Prediction',
            data: [prediction.predicted_hires || 0, prediction.predicted_turnover || 0],
            backgroundColor: ['rgba(75, 192, 192, 0.6)', 'rgba(255, 99, 132, 0.6)'],
            borderColor: ['rgba(75, 192, 192, 1)', 'rgba(255, 99, 132, 1)'],
            borderWidth: 1,
          },
        ],
      }
    : null;

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      y: { beginAtZero: true, title: { display: true, text: 'Count' } },
      x: { title: { display: true, text: 'Metrics' } },
    },
    plugins: {
      legend: { display: true },
      title: { display: true, text: 'Workforce Prediction Analysis' },
    },
  };

  return (
    <div className="recruiter-welcome-container">
      <section className="recruiter-welcome-section">
        <div className="recruiter-welcome-header">
          <h2>Welcome, {profile?.companyName || 'Recruiter'}!</h2>
          <p>Manage your hiring process with HireSphere</p>
        </div>
        <div className="recruiter-quick-links">
          <Link to="/create-job" className="recruiter-action-link">
            <span>📝</span> Post New Job
          </Link>
          <Link to="/my-jobs" className="recruiter-action-link">
            <span>📋</span> My Jobs
          </Link>
          <Link to="/profile" className="recruiter-action-link">
            <span>👤</span> Profile
          </Link>
        </div>
      </section>

      {Object.values(errors).some((e) => e) && (
        <div className="error-container">
          {Object.entries(errors).map(([key, value]) => value && <ErrorMessage key={key} message={value} />)}
        </div>
      )}

      <div className="recruiter-dashboard">
        <section className="recruiter-recommendations-section">
          <h3>Recent Job Postings</h3>
          {loading.jobs ? (
            <p className="recruiter-loading">Loading...</p>
          ) : recentJobs.length > 0 ? (
            <ul className="recruiter-recommendations-list">
              {recentJobs.map((job) => (
                <li key={job.id} className="recruiter-job-item">
                  <div className="recruiter-job-info">
                    <Link to={`/jobs/${job.id}/applications`} className="recruiter-job-title">{job.title}</Link>
                    <p className="recruiter-job-location">{job.location}</p>
                  </div>
                  <div className="recruiter-job-actions">
                    <span className="recruiter-match-score">
                      {job.applications} Application{job.applications !== 1 ? 's' : ''}
                    </span>
                    <Link to={`/jobs/${job.id}/applications`} className="recruiter-apply-btn">View Applications</Link>
                  </div>
                </li>
              ))}
            </ul>
          ) : (
            <div className="recruiter-no-recommendations">
              <h3>No Recent Job Postings</h3>
              <p>Start by creating your first job posting</p>
              <Link to="/create-job" className="recruiter-apply-btn">Create Job</Link>
            </div>
          )}
        </section>

        <section className="recruiter-recent-applications">
          <h3>Application Summary</h3>
          {loading.applications ? (
            <p className="recruiter-loading">Loading...</p>
          ) : (
            <div className="recruiter-applications-list">
              <div className="recruiter-application-item">
                <div className="recruiter-application-link">
                  <span className="recruiter-application-title">New Applications (Last 7 Days)</span>
                  <span className="recruiter-application-status pending">{applicationSummary.newApplications}</span>
                </div>
              </div>
            </div>
          )}
        </section>

        <section className="recruiter-recommendations-section">
          <h3>Dashboard Metrics</h3>
          <div className="metrics-filter">
            <label htmlFor="period-select">Select Period:</label>
            <select id="period-select" value={period} onChange={handlePeriodChange} className="metrics-period-select">
              <option value="daily">Daily</option>
              <option value="weekly">Weekly</option>
              <option value="monthly">Monthly</option>
            </select>
          </div>
          {loading.metrics ? (
            <p className="recruiter-loading">Loading metrics...</p>
          ) : errors.metrics ? (
            <div className="error-message">
              <p>{errors.metrics}</p>
              <button onClick={fetchMetrics} className="retry-button">Retry</button>
            </div>
          ) : metrics.length > 0 ? (
            <div className="metrics-cards">
              {metrics.map((metric, index) => (
                <div key={index} className="metric-card">
                  <h4>{new Date(metric.metricDate).toLocaleDateString() || 'Metrics'}</h4>
                  <div className="metric-details">
                    <div className="metric-item">
                      <span className="metric-label">Total Applications</span>
                      <span className="metric-value">{metric.totalApplications || 0}</span>
                    </div>
                    <div className="metric-item">
                      <span className="metric-label">Shortlisted Candidates</span>
                      <span className="metric-value">{metric.shortlistedCandidates || 0}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="recruiter-no-recommendations">
              <p>No metrics available for this period.</p>
            </div>
          )}
        </section>

        <section className="recruiter-recommendations-section">
          <h3>Workforce Prediction</h3>
          <div className="prediction-upload">
            <input
              type="file"
              accept=".csv"
              onChange={handleFileChange}
              className="prediction-file-input"
              disabled={loading.prediction}
            />
            <button
              onClick={handlePredict}
              className="recruiter-apply-btn"
              disabled={loading.prediction || !csvFile}
            >
              {loading.prediction ? 'Processing...' : 'Generate Prediction'}
            </button>
            {errors.prediction && <ErrorMessage message={errors.prediction} />}
          </div>
          {prediction && (
            <div className="prediction-results">
              <div className="prediction-chart">
                <Bar data={chartData} options={chartOptions} />
              </div>
              {prediction.skills_demand && (
                <div className="skills-demand">
                  <h4>Skills Demand</h4>
                  <ul className="skills-list">
                    {Object.entries(prediction.skills_demand).map(([skill, demand]) => (
                      <li key={skill} className="skill-item">
                        <span className="skill-name">{skill}</span>
                        <span className="skill-demand">{(demand * 100).toFixed(1)}%</span>
                      </li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          )}
        </section>
      </div>
      <Footer />
    </div>
  );
};

export default RecruiterWelcome;