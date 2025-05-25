import React, { useState, useEffect } from 'react';
import api from '../service/api';
import ErrorMessage from '../components/ErrorMessage';
import '../style.css'; 

const Privacy = () => {
  const [content, setContent] = useState('');
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchPrivacyNotice = async () => {
      try {
        const res = await api.get('/privacy', {
          headers: { 'Content-Type': 'text/html' },
        });
        setContent(res.data);
      } catch (err) {
        setError('Failed to load privacy notice: ' + (err.response?.data?.error || err.message));
        console.error('Error fetching privacy notice:', err);
      }
    };
    fetchPrivacyNotice();
  }, []);

  if (error) {
    return (
      <div className="privacy-container">
        <h2>Privacy Notice</h2>
        <ErrorMessage message={error} />
      </div>
    );
  }

  return (
    <div className="privacy-container">
      <h2>Privacy Notice</h2>
      {content ? (
        <div
          className="privacy-content"
          dangerouslySetInnerHTML={{ __html: content }}
        />
      ) : (
        <p>Loading privacy notice...</p>
      )}
    </div>
  );
};

export default Privacy;