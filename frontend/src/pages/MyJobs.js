import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../service/api'; 

const MyJobs = ({ auth }) => {
  const [jobs, setJobs] = useState([]);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchJobs = async () => {
      try {
        const res = await api.get('/recruiters/jobs', {
          headers: { Authorization: `Bearer ${auth.token}` },
        });
        setJobs(res.data);
      } catch (err) {
        setError(err.response?.data?.error || 'Failed to fetch jobs');
      }
    };
    fetchJobs();
  }, [auth.token]);

  const handleViewApplications = (jobId) => {
    navigate(`/jobs/${jobId}/applications`); 
  };

  return (
    <div className="my-jobs-container">
      <h2>My Job Postings</h2>
      {error && <p className="error">{error}</p>}
      {jobs.length > 0 ? (
        <div className="job-list">
          {jobs.map((job) => (
            <div key={job.id} className="job-card">
              <h3>{job.title}</h3>
              <p><strong>Location:</strong> {job.location || 'Not specified'}</p>
              <p><strong>Work Type:</strong> {job.workType || 'Not specified'}</p>
              <p><strong>Salary Range:</strong> {job.salaryRange || 'Not specified'}</p>
              <p><strong>Required Skills:</strong> {job.requiredSkills?.join(', ') || 'None'}</p>
              <p>{job.description}</p>
              <button
                onClick={() => handleViewApplications(job.id)}
                className="btn"
              >
                View Applications
              </button>
            </div>
          ))}
        </div>
      ) : (
        <p>No jobs posted yet. <Link to="/create-job">Create one now</Link>.</p>
      )}
    </div>
  );
};

export default MyJobs;