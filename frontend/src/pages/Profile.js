import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom'; 
import api from '../service/api';
import ErrorMessage from '../components/ErrorMessage';

const Profile = ({ auth }) => {
  const [profile, setProfile] = useState(null);
  const [userData, setUserData] = useState(null); 
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({});
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate(); 

  const isRecruiter = auth.role === 'recruiter';
  const endpoint = isRecruiter ? '/recruiters/me' : '/candidates/me';
  const updateEndpoint = isRecruiter ? '/recruiters/update-me' : '/candidates/update-me';

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const dataEndpoint = isRecruiter ? '/recruiters/data' : '/candidates/data';
        const [profileRes, dataRes] = await Promise.all([
          api.get(endpoint, { headers: { Authorization: `Bearer ${auth.token}` } }),
          api.get(dataEndpoint, { headers: { Authorization: `Bearer ${auth.token}` } }),
        ]);
        console.log('Profile Response:', profileRes.data); 
        console.log('GDPR Data Response:', dataRes.data); 
        setProfile(profileRes.data);
        setFormData(profileRes.data);
        setUserData(dataRes.data);
        setError(null); 
      } catch (err) {
        const errorMsg = err.response?.status === 403 
          ? 'Access denied. Check your authentication or permissions.'
          : err.response?.data?.error || 'Failed to fetch profile';
        setError(errorMsg);
        console.error('Fetch error:', err.response || err);
      } finally {
        setLoading(false); 
      }
    };
    fetchProfile();
  }, [auth.token, endpoint, isRecruiter]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const updatedData = {
        ...formData,
        ...(isRecruiter
          ? { companySize: parseInt(formData.companySize) || 0 }
          : {
              yearsOfExperience: parseInt(formData.yearsOfExperience) || 0,
              previousCompanies: parseInt(formData.previousCompanies) || 0,
              age: parseInt(formData.age) || 0,
              skills:
                typeof formData.skills === 'string'
                  ? formData.skills.split(',').map((s) => s.trim())
                  : formData.skills || profile.skills,
            }),
      };
      const res = await api.put(updateEndpoint, updatedData, {
        headers: { Authorization: `Bearer ${auth.token}` },
      });
      setProfile(res.data);
      setIsEditing(false);
      setError(null);
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to update profile');
      console.error('Update error:', err.response || err);
    }
  };

  const handleDeleteData = async () => {
    if (window.confirm('Are you sure you want to delete all your data? This action cannot be undone.')) {
      try {
        await api.delete('/candidates/data', {
          headers: { Authorization: `Bearer ${auth.token}` },
        });
        navigate('/logout'); // Redirect to logout or login page
      } catch (err) {
        setError('Failed to delete data. Please try again.');
        console.error('Delete error:', err.response || err);
      }
    }
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div className="profile-container">
      <h2>{isRecruiter ? 'Recruiter Profile' : 'Candidate Profile'}</h2>
      <ErrorMessage message={error} />
      {isEditing ? (
        <form onSubmit={handleSubmit} className="profile-form">
          {isRecruiter ? (
            <>
              <div className="form-group">
                <label>Company Name</label>
                <input
                  type="text"
                  name="companyName"
                  value={formData.companyName || ''}
                  onChange={handleChange}
                  required
                />
              </div>
              <div className="form-group">
                <label>Company Size</label>
                <input
                  type="number"
                  name="companySize"
                  value={formData.companySize || ''}
                  onChange={handleChange}
                />
              </div>
              <div className="form-group">
                <label>Industry</label>
                <input
                  type="text"
                  name="industry"
                  value={formData.industry || ''}
                  onChange={handleChange}
                />
              </div>
              <div className="form-group">
                <label>Location</label>
                <input
                  type="text"
                  name="location"
                  value={formData.location || ''}
                  onChange={handleChange}
                />
              </div>
              <div className="form-group">
                <label>Company Website</label>
                <input
                  type="text"
                  name="companyWebsite"
                  value={formData.companyWebsite || ''}
                  onChange={handleChange}
                />
              </div>
              <div className="form-group">
                <label>Contact Email</label>
                <input
                  type="email"
                  name="contactEmail"
                  value={formData.contactEmail || ''}
                  onChange={handleChange}
                />
              </div>
              <div className="form-group">
                <label>Company Description</label>
                <textarea
                  name="companyDescription"
                  value={formData.companyDescription || ''}
                  onChange={handleChange}
                />
              </div>
              <div className="form-group">
                <label>Hiring Preferences</label>
                <input
                  type="text"
                  name="hiringPreferences"
                  value={formData.hiringPreferences || ''}
                  onChange={handleChange}
                />
              </div>
            </>
          ) : (
            <>
              <div className="form-group">
                <label>Name</label>
                <input
                  type="text"
                  name="name"
                  value={formData.name || ''}
                  onChange={handleChange}
                  required
                />
              </div>
              <div className="form-group">
                <label>Location</label>
                <input
                  type="text"
                  name="location"
                  value={formData.location || ''}
                  onChange={handleChange}
                />
              </div>
              <div className="form-group">
                <label>Education Level</label>
                <input
                  type="text"
                  name="educationLevel"
                  value={formData.educationLevel || ''}
                  onChange={handleChange}
                />
              </div>
              <div className="form-group">
                <label>Years of Experience</label>
                <input
                  type="number"
                  name="yearsOfExperience"
                  value={formData.yearsOfExperience || ''}
                  onChange={handleChange}
                />
              </div>
              <div className="form-group">
                <label>Previous Companies</label>
                <input
                  type="number"
                  name="previousCompanies"
                  value={formData.previousCompanies || ''}
                  onChange={handleChange}
                />
              </div>
              <div className="form-group">
                <label>Preferred Job Types</label>
                <input
                  type="text"
                  name="preferredJobTypes"
                  value={formData.preferredJobTypes || ''}
                  onChange={handleChange}
                />
              </div>
              <div className="form-group">
                <label>Availability</label>
                <input
                  type="text"
                  name="availability"
                  value={formData.availability || ''}
                  onChange={handleChange}
                />
              </div>
              <div className="form-group">
                <label>Skills (comma-separated)</label>
                <input
                  type="text"
                  name="skills"
                  value={formData.skills?.join(', ') || ''}
                  onChange={handleChange}
                />
              </div>
              <div className="form-group">
                <label>Gender</label>
                <input
                  type="text"
                  name="gender"
                  value={formData.gender || ''}
                  onChange={handleChange}
                />
              </div>
              <div className="form-group">
                <label>Age</label>
                <input
                  type="number"
                  name="age"
                  value={formData.age || ''}
                  onChange={handleChange}
                />
              </div>
            </>
          )}
          <div className="form-actions">
            <button type="submit" className="btn">Save</button>
            <button type="button" className="btn" onClick={() => setIsEditing(false)}>Cancel</button>
          </div>
        </form>
      ) : (
        <div className="profile-view">
          {isRecruiter ? (
            <>
              <p><strong>Company Name:</strong> {profile.companyName}</p>
              <p><strong>Company Size:</strong> {profile.companySize}</p>
              <p><strong>Industry:</strong> {profile.industry}</p>
              <p><strong>Location:</strong> {profile.location}</p>
              <p><strong>Website:</strong> {profile.companyWebsite}</p>
              <p><strong>Contact Email:</strong> {profile.contactEmail}</p>
              <p><strong>Description:</strong> {profile.companyDescription}</p>
              <p><strong>Hiring Preferences:</strong> {profile.hiringPreferences}</p>
            </>
          ) : (
            <>
              <p><strong>Name:</strong> {profile.name}</p>
              <p><strong>Location:</strong> {profile.location}</p>
              <p><strong>Education Level:</strong> {profile.educationLevel}</p>
              <p><strong>Years of Experience:</strong> {profile.yearsOfExperience}</p>
              <p><strong>Previous Companies:</strong> {profile.previousCompanies}</p>
              <p><strong>Preferred Job Types:</strong> {profile.preferredJobTypes}</p>
              <p><strong>Availability:</strong> {profile.availability}</p>
              <p><strong>Skills:</strong> {profile.skills?.join(', ')}</p>
              <p><strong>Gender:</strong> {profile.gender}</p>
              <p><strong>Age:</strong> {profile.age}</p>
            
              {userData && (
                <div className="gdpr-data">
                  <h3>Your Data Summary</h3>
                  <p><strong>Skills Stored:</strong> {userData.skills?.length || 0}</p>
                  <p><strong>Applications Submitted:</strong> {userData.applications?.length || 0}</p>
                </div>
              )}
            </>
          )}
          <div className="profile-actions">
            <button className="btn" onClick={() => setIsEditing(true)}>Edit Profile</button>
            {!isRecruiter && (
              <button className="btn delete-btn" onClick={handleDeleteData}>Delete My Data</button>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default Profile;