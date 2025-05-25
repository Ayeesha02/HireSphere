import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../service/api';
import ErrorMessage from '../components/ErrorMessage';

const MyApplications = ({ auth }) => {
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchApplications = async () => {
      setLoading(true);
      try {
        const res = await api.get('/candidates/applications', {
          headers: { Authorization: `Bearer ${auth.token}` },
        });
        setApplications(res.data);
      } catch (err) {
        setError('Failed to load applications.');
        console.error('Error fetching applications:', err.response || err);
      } finally {
        setLoading(false);
      }
    };
    fetchApplications();
  }, [auth.token]);

  return (
        <div className="my-applications-container">
        <h2>My Applications</h2>
        <ErrorMessage message={error} />
        {(() => {
            if (loading) {
                return <p>Loading...</p>;
            }
            if (applications.length > 0) {
                return (
                    <ul className="my-applications-list">
                        {applications.map((app) => (
                            <li key={app.id} className="my-application-item">
                                <Link to={`/interview/${app.id}`}>
                                    {app.job.title} - Status: {app.status || 'Pending'}
                                </Link>
                               
                                <p><strong>Applied On:</strong> {new Date(app.applicationDate).toLocaleDateString()}</p>
                            </li>
                        ))}
                    </ul>
                );
            }
            return <p>No applications yet.</p>;
        })()}
        </div>
  );
};

export default MyApplications;