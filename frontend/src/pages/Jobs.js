import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../service/api';
import ErrorMessage from '../components/ErrorMessage';

const Jobs = ({ auth }) => {
  const [jobs, setJobs] = useState([]);
  const [filteredJobs, setFilteredJobs] = useState([]);
  const [filters, setFilters] = useState({
    keyword: '',
    location: '',
    workType: '',
    salaryRange: '',
  });
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchAllJobs = async () => {
      try {
        const res = await api.get('/candidates/All-jobs', {
          headers: { Authorization: `Bearer ${auth.token}` },
        });
        setJobs(res.data);
        setFilteredJobs(res.data);
      } catch (err) {
        setError(err.response?.data?.error || 'Failed to fetch jobs');
        console.error(err);
      }
    };
    fetchAllJobs();
  }, [auth.token]);

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters((prev) => ({ ...prev, [name]: value }));
  };

  const applyFilters = async () => {
    try {
      const res = await api.get('/candidates/jobs/search', {
        params: filters,
        headers: { Authorization: `Bearer ${auth.token}` },
      });
      setFilteredJobs(res.data);
      setError(null);
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to filter jobs');
      setFilteredJobs(jobs);
      console.error(err);
    }
  };

  const handleApplyJob = (jobId) => {
    navigate(`/apply/${jobId}`); 
  };

  return (
    <div className="jobs-container">
      <h2>All Jobs</h2>
      
      <ErrorMessage message={error} />
      <div className="jobs-layout">
        <div className="filters">
          <h3>Filters</h3>
          <div className="filter-group">
            <label>Keyword</label>
            <input
              type="text"
              name="keyword"
              value={filters.keyword}
              onChange={handleFilterChange}
              placeholder="e.g., Software Engineer"
            />
          </div>
          <div className="filter-group">
            <label>Location</label>
            <input
              type="text"
              name="location"
              value={filters.location}
              onChange={handleFilterChange}
              placeholder="e.g., Remote"
            />
          </div>
          <div className="filter-group">
            <label>Work Type</label>
            <select name="workType" value={filters.workType} onChange={handleFilterChange}>
              <option value="">Any</option>
              <option value="Full-time">Full-time</option>
              <option value="Part-time">Part-time</option>
              <option value="Contract">Contract</option>
              <option value="Remote">Remote</option>
            </select>
          </div>
          <div className="filter-group">
            <label>Salary Range</label>
            <input
              type="text"
              name="salaryRange"
              value={filters.salaryRange}
              onChange={handleFilterChange}
              placeholder="e.g., $50k-$100k"
            />
          </div>
          <button className="btn" onClick={applyFilters}>
            Apply Filters
          </button>
        </div>
        <div className="job-list">
          {filteredJobs.length > 0 ? (
            filteredJobs.map((job) => (
              <div key={job.id} className="job-card">
                <h3>
                  <Link to={`/jobs/${job.id}`}>{job.title}</Link>
                </h3>
                <p><strong>Location:</strong> {job.location || 'Not specified'}</p>
                <p><strong>Work Type:</strong> {job.workType || 'Not specified'}</p>
                <p><strong>Salary Range:</strong> {job.salaryRange || 'Not specified'}</p>
                <p><strong>Required Skills:</strong> {job.requiredSkills?.join(', ') || 'None'}</p>
                <p>{job.description}</p>
                <button onClick={() => handleApplyJob(job.id)} className="btn">
                  Apply
                </button>
              </div>
            ))
          ) : (
            <p>No jobs match your filters.</p>
          )}
        </div>
      </div>
    </div>
  );
};

export default Jobs;