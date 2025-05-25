import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../service/api';
import ErrorMessage from '../components/ErrorMessage';

const ApplyJob = ({ auth }) => {
  const { jobId } = useParams();
  const [job, setJob] = useState(null);
  const [resume, setResume] = useState(null);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchJobDetails = async () => {
      try {
        const res = await api.get(`/candidates/jobs/${jobId}`, {
          headers: { Authorization: `Bearer ${auth.token}` },
        });
        console.log('Job details response:', res.data);
        setJob(res.data);
      } catch (err) {
        setError(err.response?.data?.error || 'Failed to fetch job details');
        console.error('Fetch job details error:', err);
      }
    };
    fetchJobDetails();
  }, [auth.token, jobId]);

  const handleResumeChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setResume(file);
      console.log('Selected file:', file.name);
    }
  };

  const handleApply = async (e) => {
    e.preventDefault();
    if (!resume) {
      setError('Please upload your resume.');
      return;
    }

    console.log('Auth token:', auth.token); 

    const formData = new FormData();
    formData.append('resume', resume);

    try {
      const res = await api.post(`/candidates/apply/${jobId}`, formData, {
        headers: {
          Authorization: `Bearer ${auth.token}`,
          'Content-Type': 'multipart/form-data',
        },
      });
      console.log('Apply response:', res.data);
      setError(null);
      
      if (window.confirm('Application submitted successfully! Last Step: Take the Interview. Proceed now?')) {
        navigate(`/interview/${res.data.application.id}`);
      } else {
        navigate('/jobs');
      }
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to apply for job');
      console.error('Apply error:', err);
    }
  };

  if (!job) return <div>Loading...</div>;

  return (
    <div className="job-details-container">
      <h2>{job.title}</h2>
      <ErrorMessage message={error} />
      <div className="job-details">
        <p><strong>Location:</strong> {job.location || 'Not specified'}</p>
        <p><strong>Work Type:</strong> {job.workType || 'Not specified'}</p>
        <p><strong>Salary Range:</strong> {job.salaryRange || 'Not specified'}</p>
        <p><strong>Required Skills:</strong> {job.requiredSkills?.join(', ') || 'None'}</p>
        <p><strong>Preferred Qualifications:</strong> {job.preferredQualifications?.join(', ') || 'None'}</p>
        <p><strong>Description:</strong> {job.description}</p>
      </div>
      <div className="apply-section">
        <h3>Apply for this Job</h3>
        <form onSubmit={handleApply}>
          <div className="form-group">
            <label>Upload Resume</label>
            <input
              type="file"
              accept=".pdf,.doc,.docx,.txt"
              onChange={handleResumeChange}
              required
            />
            {resume && <p>Selected: {resume.name}</p>}
          </div>
          <button type="submit" className="btn">Submit Application</button>
        </form>
      </div>
    </div>
  );
};

export default ApplyJob;