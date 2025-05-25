import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { signUp, login } from '../service/auth';
import ErrorMessage from '../components/ErrorMessage';
import '../style.css'; // Ensure styles are consistent with the Login page

const SignUp = ({ login: authLogin }) => {
  const [role, setRole] = useState('candidate');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [consentGiven, setConsentGiven] = useState(false);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  // Candidate fields
  const [name, setName] = useState('');
  const [location, setLocation] = useState('');
  const [educationLevel, setEducationLevel] = useState('');
  const [yearsOfExperience, setYearsOfExperience] = useState('');
  const [previousCompanies, setPreviousCompanies] = useState('');
  const [preferredJobTypes, setPreferredJobTypes] = useState('');
  const [availability, setAvailability] = useState('');
  const [skills, setSkills] = useState('');
  const [gender, setGender] = useState('');
  const [age, setAge] = useState('');

  // Recruiter fields
  const [companyName, setCompanyName] = useState('');
  const [companySize, setCompanySize] = useState('');
  const [industry, setIndustry] = useState('');
  const [companyWebsite, setCompanyWebsite] = useState('');
  const [contactEmail, setContactEmail] = useState('');
  const [companyDescription, setCompanyDescription] = useState('');
  const [hiringPreferences, setHiringPreferences] = useState('');

  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    // Validation
    if (!email || !password) {
      setError('Please enter both email and password.');
      setLoading(false);
      return;
    }

    if (!consentGiven) {
      setError('You must consent to data processing (GDPR compliance).');
      setLoading(false);
      return;
    }

    // Additional validation
    if (!email.includes('@') || !email.includes('.')) {
      setError('Please enter a valid email address.');
      setLoading(false);
      return;
    }

    if (password.length < 6) {
      setError('Password must be at least 6 characters long.');
      setLoading(false);
      return;
    }

    if (role === 'candidate' && !name) {
      setError('Name is required for candidates.');
      setLoading(false);
      return;
    }

    if (role === 'recruiter' && !companyName) {
      setError('Company name is required for recruiters.');
      setLoading(false);
      return;
    }

    const data = {
      email,
      password,
      consentGiven,
      ...(role === 'candidate'
        ? {
            name,
            location,
            educationLevel,
            yearsOfExperience: parseInt(yearsOfExperience) || 0,
            previousCompanies: parseInt(previousCompanies) || 0,
            preferredJobTypes,
            availability,
            skills: skills ? skills.split(',').map((skill) => skill.trim()).filter(Boolean) : [],
            gender: gender || 'Unknown', // Default to "Unknown" per UserProfile
            age: parseInt(age) || 0,
          }
        : {
            companyName,
            companySize: parseInt(companySize) || 0,
            industry,
            location,
            companyWebsite,
            contactEmail,
            companyDescription,
            hiringPreferences,
          }),
    };

    try {
      await signUp(data, role);
      const token = await login(email, password, role);
      authLogin(token, role);

      // Navigate based on role, matching Login page behavior
      switch (role.toLowerCase()) {
        case 'recruiter':
          navigate('/recruiter/welcome');
          break;
        case 'candidate':
          navigate('/candidate/welcome');
          break;
        default:
          setError('Unknown role selected.');
          break;
      }
    } catch (err) {
      setError(err.message || err.response?.data?.error || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <h2>Register with HireSphere</h2>
      <ErrorMessage message={error} />
      {loading && <p className="loading">Registering...</p>}
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

        {role === 'candidate' ? (
          <>
            <div className="form-group">
              <label>Name <span className="required">*</span></label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Enter your full name"
                required
                disabled={loading}
              />
            </div>
            <div className="form-group">
              <label>Location</label>
              <input
                type="text"
                value={location}
                onChange={(e) => setLocation(e.target.value)}
                placeholder="e.g., New York"
                disabled={loading}
              />
            </div>
            <div className="form-group">
              <label>Education Level</label>
              <select
                value={educationLevel}
                onChange={(e) => setEducationLevel(e.target.value)}
                disabled={loading}
              >
                <option value="">Select education level</option>
                <option value="High School">High School</option>
                <option value="Bachelor's">Bachelor's</option>
                <option value="Master's">Master's</option>
                <option value="PhD">PhD</option>
              </select>
            </div>
            <div className="form-group">
              <label>Years of Experience</label>
              <input
                type="number"
                min="0"
                value={yearsOfExperience}
                onChange={(e) => setYearsOfExperience(e.target.value)}
                placeholder="e.g., 3"
                disabled={loading}
              />
            </div>
            <div className="form-group">
              <label>Number of Previous Companies</label>
              <input
                type="number"
                min="0"
                value={previousCompanies}
                onChange={(e) => setPreviousCompanies(e.target.value)}
                placeholder="e.g., 2"
                disabled={loading}
              />
            </div>
            <div className="form-group">
              <label>Preferred Job Types</label>
              <select
                value={preferredJobTypes}
                onChange={(e) => setPreferredJobTypes(e.target.value)}
                disabled={loading}
              >
                <option value="">Select job type</option>
                <option value="Full-time">Full-time</option>
                <option value="Remote">Remote</option>
                <option value="Part-time">Part-time</option>
                <option value="Contract">Contract</option>
                <option value="Full-time, Remote">Full-time, Remote</option>
                <option value="Hybrid">Hybrid</option>
              </select>
            </div>
            <div className="form-group">
              <label>Availability</label>
              <select
                value={availability}
                onChange={(e) => setAvailability(e.target.value)}
                disabled={loading}
              >
                <option value="">Select availability</option>
                <option value="Immediate">Immediate</option>
                <option value="1 week notice">1 week notice</option>
                <option value="2 weeks notice">2 weeks notice</option>
                <option value="1 month notice">1 month notice</option>
              </select>
            </div>
            <div className="form-group">
              <label>Skills (comma-separated)</label>
              <input
                type="text"
                value={skills}
                onChange={(e) => setSkills(e.target.value)}
                placeholder="e.g., Java, React, SQL"
                disabled={loading}
              />
              <small>Enter skills separated by commas</small>
            </div>
            <div className="form-group">
              <label>Gender</label>
              <select
                value={gender}
                onChange={(e) => setGender(e.target.value)}
                disabled={loading}
              >
                <option value="">Select gender (optional)</option>
                <option value="Male">Male</option>
                <option value="Female">Female</option>
              </select>
            </div>
            <div className="form-group">
              <label>Age</label>
              <input
                type="number"
                min="0"
                value={age}
                onChange={(e) => setAge(e.target.value)}
                placeholder="Enter your age"
                disabled={loading}
              />
            </div>
          </>
        ) : (
          <>
            <div className="form-group">
              <label>Company Name <span className="required">*</span></label>
              <input
                type="text"
                value={companyName}
                onChange={(e) => setCompanyName(e.target.value)}
                placeholder="Enter company name"
                required
                disabled={loading}
              />
            </div>
            <div className="form-group">
              <label>Company Size</label>
              <input
                type="number"
                min="0"
                value={companySize}
                onChange={(e) => setCompanySize(e.target.value)}
                placeholder="Number of employees"
                disabled={loading}
              />
            </div>
            <div className="form-group">
              <label>Industry</label>
              <input
                type="text"
                value={industry}
                onChange={(e) => setIndustry(e.target.value)}
                placeholder="e.g., Technology"
                disabled={loading}
              />
            </div>
            <div className="form-group">
              <label>Location</label>
              <input
                type="text"
                value={location}
                onChange={(e) => setLocation(e.target.value)}
                placeholder="e.g., New York"
                disabled={loading}
              />
            </div>
            <div className="form-group">
              <label>Company Website</label>
              <input
                type="url"
                value={companyWebsite}
                onChange={(e) => setCompanyWebsite(e.target.value)}
                placeholder="https://example.com"
                disabled={loading}
              />
            </div>
            <div className="form-group">
              <label>Contact Email</label>
              <input
                type="email"
                value={contactEmail}
                onChange={(e) => setContactEmail(e.target.value)}
                placeholder="contact@company.com"
                disabled={loading}
              />
            </div>
            <div className="form-group">
              <label>Company Description</label>
              <textarea
                value={companyDescription}
                onChange={(e) => setCompanyDescription(e.target.value)}
                placeholder="Describe your company..."
                disabled={loading}
                rows="4"
              />
            </div>
            <div className="form-group">
              <label>Hiring Preferences</label>
              <textarea
                value={hiringPreferences}
                onChange={(e) => setHiringPreferences(e.target.value)}
                placeholder="Describe your hiring preferences..."
                disabled={loading}
                rows="4"
              />
            </div>
          </>
        )}

        <div className="form-group consent">
          <input
            type="checkbox"
            id="consent"
            checked={consentGiven}
            onChange={(e) => setConsentGiven(e.target.checked)}
            disabled={loading}
          />
          <label htmlFor="consent">
            I consent to the processing of my data in accordance with GDPR regulations.
          </label>
        </div>

        <button type="submit" className="btn" disabled={loading}>
          {loading ? 'Registering...' : 'Register'}
        </button>
      </form>
      <p>
        Already have an account? <Link to="/login">Login here</Link>
      </p>
    </div>
  );
};

export default SignUp;