import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../service/api';
import ErrorMessage from '../components/ErrorMessage';

const CreateJob = ({ auth }) => {
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    location: '',
    requiredSkills: '',
    preferredQualifications: '',
    workType: '',
    salaryRange: '',
    recruitmentStrategy: ''
  });
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const jobData = {
        ...formData,
        requiredSkills: formData.requiredSkills.split(',').map((skill) => skill.trim()),
        preferredQualifications: formData.preferredQualifications.split(',').map((qual) => qual.trim()),
      };
      await api.post('/recruiters/jobs', jobData, {
        headers: { Authorization: `Bearer ${auth.token}` },
      });
      navigate('/my-jobs'); // Redirect to My Jobs after success
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to create job');
    }
  };

  return (
    <div className="create-job-container">
      <h2>Create a New Job Posting</h2>
      <ErrorMessage message={error} />
      <form onSubmit={handleSubmit} className="job-form">
        <div className="form-group">
          <label>Job Title</label>
          <input
            type="text"
            name="title"
            value={formData.title}
            onChange={handleChange}
            placeholder="e.g., Senior Software Engineer"
            required
          />
        </div>
        <div className="form-group">
          <label>Description</label>
          <textarea
            name="description"
            value={formData.description}
            onChange={handleChange}
            placeholder="Describe the job responsibilities and requirements"
            required
          />
        </div>
        <div className="form-group">
          <label>Location</label>
          <input
            type="text"
            name="location"
            value={formData.location}
            onChange={handleChange}
            placeholder="e.g., San Francisco, CA or Remote"
          />
        </div>
        <div className="form-group">
          <label>Required Skills (comma-separated)</label>
          <input
            type="text"
            name="requiredSkills"
            value={formData.requiredSkills}
            onChange={handleChange}
            placeholder="e.g., Java, React, SQL"
          />
        </div>
        <div className="form-group">
          <label>Preferred Qualifications (comma-separated)</label>
          <input
            type="text"
            name="preferredQualifications"
            value={formData.preferredQualifications}
            onChange={handleChange}
            placeholder="e.g., 5+ years experience, AWS certification"
          />
        </div>
        <div className="form-group">
          <label>Work Type</label>
          <select name="workType" value={formData.workType} onChange={handleChange}>
            <option value="">Select Work Type</option>
            <option value="Full-time">Full-time</option>
            <option value="Part-time">Part-time</option>
            <option value="Contract">Contract</option>
            <option value="Remote">Remote</option>
          </select>
        </div>
        <div className="form-group">
          <label>Salary Range</label>
          <input
            type="text"
            name="salaryRange"
            value={formData.salaryRange}
            onChange={handleChange}
            placeholder="e.g., $80k-$120k"
          />
        </div>
        <button type="submit" className="btn">Post Job</button>
      </form>
    </div>
  );
};

export default CreateJob;