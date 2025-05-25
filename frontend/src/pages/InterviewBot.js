import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../service/api';
import '../style.css'; 

const InterviewPage = ({ auth }) => {
  const { applicationId } = useParams();
  const navigate = useNavigate();
  const hasStarted = useRef(false);
  const chatEndRef = useRef(null); 

  const [conversation, setConversation] = useState([]);
  const [answer, setAnswer] = useState('');
  const [isComplete, setIsComplete] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (hasStarted.current) return;
    hasStarted.current = true;

    const startInterview = async () => {
      try {
        setLoading(true);
        setError(null);
        const { data } = await api.post(
          `/candidates/start?applicationId=${applicationId}`,
          {},
          { headers: { Authorization: `Bearer ${auth.token}` } }
        );
        console.log('First question:', data);
        setConversation([{ type: 'question', text: data }]);
      } catch (err) {
        setError(err.response?.data || 'Failed to start the interview.');
        console.error('Start interview error:', err);
      } finally {
        setLoading(false);
      }
    };
    startInterview();
  }, [applicationId, auth.token]);

  useEffect(() => {
    
    if (chatEndRef.current) {
      chatEndRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }, [conversation, loading]);

  const handleSubmit = async () => {
    if (!answer.trim() || loading) return;

    try {
      setLoading(true);
      setError(null);

      const updatedConversation = [...conversation, { type: 'answer', text: answer }];
      setConversation(updatedConversation);

      await api.post(
        `/candidates/${applicationId}/submit`,
        { response: answer },
        { headers: { Authorization: `Bearer ${auth.token}` } }
      );

      const { data } = await api.get(`/candidates/${applicationId}/next-question`, {
        headers: { Authorization: `Bearer ${auth.token}` },
      });
      console.log('Next question:', data);

      if (data === 'Interview completed.') {
        await api.post(
          `/candidates/applications/${applicationId}/complete-interview`,
          {},
          { headers: { Authorization: `Bearer ${auth.token}` } }
        );
        setIsComplete(true);
        setTimeout(() => {
          alert('Thank you for completing the interview!');
          navigate('/jobs');
        }, 2000);
      } else {
        setConversation([...updatedConversation, { type: 'question', text: data }]);
      }

      setAnswer('');
    } catch (err) {
      setError(err.response?.data?.error || 'Error processing your answer.');
      console.error('Submit error:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading && conversation.length === 0) {
    return <div className="interview-container"><p>Starting your interview...</p></div>;
  }

  if (error) {
    return (
      <div className="interview-container">
        <div className="error">
          <p>{error}</p>
          <button className="control-button" onClick={() => window.location.reload()}>
            Retry
          </button>
        </div>
      </div>
    );
  }

  if (isComplete) {
    return (
      <div className="interview-container">
        <p>Interview completed successfully! Redirecting...</p>
      </div>
    );
  }

  return (
    <div className="interview-container">
      <div className="interview-header">
        <h1>AI-Powered Interview</h1>
        <p>Application ID: {applicationId}</p>
      </div>
      <div className="chat-container">
        <div className="chat-messages">
          {conversation.map((entry, index) => (
            <div
              key={index}
              className={`message ${entry.type === 'question' ? 'bot-message' : 'user-message'}`}
            >
              <div className="message-content">
                {entry.type === 'question' ? (
                  <><strong>Interviewer:</strong> {entry.text}</>
                ) : (
                  <><strong>You:</strong> {entry.text}</>
                )}
              </div>
              <div className="message-time">
                {new Date().toLocaleTimeString()}
              </div>
            </div>
          ))}
          {loading && (
            <div className="typing-indicator">
              <span>Interviewer is typing....</span>
              <div className="typing-dots">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          )}
          <div ref={chatEndRef} />
        </div>
        <div className="chat-input-container">
          <textarea
            className="chat-input"
            value={answer}
            onChange={(e) => setAnswer(e.target.value)}
            placeholder="Type your answer here..."
            disabled={loading}
          />
          <button
            className="send-button"
            onClick={handleSubmit}
            disabled={!answer.trim() || loading}
          >
            {loading ? 'Processing...' : 'Send'}
          </button>
        </div>
      </div>
      <div className="interview-controls">
        <button
          className="control-button end-interview"
          onClick={() => navigate('/jobs')}
        >
          End Interview
        </button>
        <span className="timer">
          {new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
        </span>
      </div>
    </div>
  );
};

export default InterviewPage;