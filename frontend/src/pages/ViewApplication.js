import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../service/api';
import ErrorMessage from '../components/ErrorMessage';
import { Document, Page, pdfjs } from 'react-pdf';
import 'react-pdf/dist/esm/Page/AnnotationLayer.css';
import 'react-pdf/dist/esm/Page/TextLayer.css';
import '../style.css';

// Set up PDF.js worker
pdfjs.GlobalWorkerOptions.workerSrc = `//cdnjs.cloudflare.com/ajax/libs/pdf.js/${pdfjs.version}/pdf.worker.min.js`;

const ViewApplication = () => {
  const { jobId } = useParams();
  const navigate = useNavigate();
  const [applications, setApplications] = useState([]);
  const [filteredApplications, setFilteredApplications] = useState([]);
  const [totalCount, setTotalCount] = useState(0);
  const [error, setError] = useState(null);
  const [topN, setTopN] = useState(0);
  const [expandedAppId, setExpandedAppId] = useState(null);
  const [selectedAppId, setSelectedAppId] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  // Check authentication status
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      setError('Please log in to view applications');
      navigate('/login');
      return;
    }
  }, [navigate]);

  const handleAuthError = (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.removeItem('token');
      setError('Your session has expired. Please log in again.');
      navigate('/login');
    } else {
      setError(error.response?.data?.error || 'An error occurred while fetching data');
    }
  };

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) return;

    const fetchApplications = async () => {
      setIsLoading(true);
      try {
        // Fetch applications for the job
        const appRes = await api.get(`/recruiters/jobs/${jobId}/applications`);
        console.log('Raw API response:', appRes);
        
  
        if (!appRes || !appRes.data) {
          console.error('Invalid API response:', appRes);
          setError('Invalid response from server');
          return;
        }

        // Handle different possible response formats
        let apps = [];
        if (Array.isArray(appRes.data)) {
          apps = appRes.data;
        } else if (appRes.data.applications && Array.isArray(appRes.data.applications)) {
          apps = appRes.data.applications;
        } else if (appRes.data.data && Array.isArray(appRes.data.data)) {
          apps = appRes.data.data;
        } else {
          console.error('Unexpected data format:', appRes.data);
          setError('Unexpected data format from server');
          return;
        }

        console.log('Processed applications:', apps);

        // Fetch AI data and resume for each application
        const appsWithScores = await Promise.all(
          apps.map(async (app) => {
            if (!app || !app.id) {
              console.warn('Invalid application data:', app);
              return null;
            }

            let aiData = { resumeScore: 0, skillMatchScore: 0, personalityScore: 0 };
            try {
              const aiRes = await api.get(`/recruiters/applications/${app.id}/ai-data`);
              aiData = aiRes.data || aiData;
            } catch (aiErr) {
              console.warn(`AI data fetch failed for app ${app.id}:`, aiErr.response?.status);
              if (aiErr.response?.status === 401 || aiErr.response?.status === 403) {
                throw new Error('Unauthorized');
              }
            }

            let resumeUrl = null;
            try {
              const resumeRes = await api.get(`/recruiters/applications/${app.id}/resume`, {
                responseType: 'blob',
              });
              resumeUrl = URL.createObjectURL(resumeRes.data);
            } catch (resumeErr) {
              console.warn(`Resume fetch failed for app ${app.id}:`, resumeErr.response?.status);
              if (resumeErr.response?.status === 401 || resumeErr.response?.status === 403) {
                throw new Error('Unauthorized');
              }
            }

            const overallScore = (aiData.resumeScore + aiData.skillMatchScore + aiData.personalityScore) / 3;
            return { ...app, aiData, overallScore, resumeUrl };
          })
        );

        // Filter out any null entries and sort by score
        const validApps = appsWithScores.filter(app => app !== null);
        const sortedApps = validApps.sort((a, b) => b.overallScore - a.overallScore);
        
        console.log('Final processed applications:', sortedApps);
        
        setApplications(sortedApps);
        setFilteredApplications(topN === 0 ? sortedApps : sortedApps.slice(0, topN));

        // Fetch total count
        const countRes = await api.get(`/recruiters/applications/count/${jobId}`);
        setTotalCount(countRes.data?.count || countRes.data || 0);
      } catch (err) {
        console.error('Fetch error:', err);
        handleAuthError(err);
      } finally {
        setIsLoading(false);
      }
    };

    fetchApplications();

    // Cleanup function
    return () => {
      applications.forEach((app) => {
        if (app?.resumeUrl) {
          URL.revokeObjectURL(app.resumeUrl);
        }
      });
    };
  }, [jobId, navigate, topN]);

  const handleStatusUpdate = async (applicationId, status) => {
    try {
      const res = await api.put(
        `/recruiters/applications/${applicationId}/status`,
        { status }
      );
      setApplications((prev) =>
        prev.map((app) => (app.id === applicationId ? { ...app, ...res.data } : app))
      );
      setFilteredApplications((prev) =>
        prev.map((app) => (app.id === applicationId ? { ...app, ...res.data } : app))
      );
    } catch (err) {
      handleAuthError(err);
    }
  };

  useEffect(() => {
    if (applications.length > 0) {
      if (topN === 0) {
        setFilteredApplications(applications);
      } else {
        const sorted = [...applications].sort((a, b) => b.overallScore - a.overallScore);
        setFilteredApplications(sorted.slice(0, topN));
      }
    }
  }, [topN, applications]);

  const handleFilterTopN = (e) => {
    const n = parseInt(e.target.value) || 0;
    setTopN(n);
  };

  const toggleDetails = (appId) => {
    setExpandedAppId(expandedAppId === appId ? null : appId);
  };

  const ResumePreview = ({ resumeUrl }) => {
    const [loading, setLoading] = useState(true);
    const [pdfError, setPdfError] = useState(null);
    const [numPages, setNumPages] = useState(null);
    const [pageNumber, setPageNumber] = useState(1);
    const [scale, setScale] = useState(1.0);

    if (!resumeUrl) return <p>No resume available</p>;

    const onDocumentLoadSuccess = ({ numPages }) => {
      setNumPages(numPages);
      setLoading(false);
    };

    const zoomIn = () => setScale(prev => Math.min(prev + 0.2, 2.0));
    const zoomOut = () => setScale(prev => Math.max(prev - 0.2, 0.5));
    const nextPage = () => setPageNumber(prev => Math.min(prev + 1, numPages));
    const prevPage = () => setPageNumber(prev => Math.max(prev - 1, 1));

    return (
      <div className="resume-preview-container">
        {loading && <p>Loading resume...</p>}
        {pdfError && <p>Error loading resume: {pdfError}</p>}
        <div className="pdf-controls">
          <button onClick={prevPage} disabled={pageNumber <= 1}>Previous</button>
          <span>Page {pageNumber} of {numPages}</span>
          <button onClick={nextPage} disabled={pageNumber >= numPages}>Next</button>
          <button onClick={zoomOut}>Zoom Out</button>
          <span>{(scale * 100).toFixed(0)}%</span>
          <button onClick={zoomIn}>Zoom In</button>
        </div>
        <Document
          file={resumeUrl}
          onLoadSuccess={onDocumentLoadSuccess}
          onLoadError={(error) => {
            console.error('PDF load error:', error);
            setLoading(false);
            setPdfError(error.message);
          }}
        >
          <Page 
            pageNumber={pageNumber} 
            scale={scale}
            width={400}
          />
        </Document>
      </div>
    );
  };

  const AIDataView = ({ aiData }) => {
    if (!aiData) return null;

    return (
      <div className="ai-data-view">
        <h4>AI Analysis Summary</h4>
        <div className="ai-data-grid">
          <div className="ai-data-item">
            <span className="ai-data-label">Resume Score</span>
            <span className="ai-data-value">{aiData.resumeScore.toFixed(1)}</span>
          </div>
          <div className="ai-data-item">
            <span className="ai-data-label">Skill Match</span>
            <span className="ai-data-value">{aiData.skillMatchScore.toFixed(1)}</span>
          </div>
          <div className="ai-data-item">
            <span className="ai-data-label">Personality Score</span>
            <span className="ai-data-value">{aiData.personalityScore.toFixed(1)}</span>
          </div>
        </div>
        {aiData.skills && (
          <div className="skills-section">
            <h5>Key Skills</h5>
            <div className="skills-list">
              {aiData.skills.map((skill, index) => (
                <span key={index} className="skill-tag">{skill}</span>
              ))}
            </div>
          </div>
        )}
      </div>
    );
  };

  if (isLoading) {
    return <div className="loading">Loading applications...</div>;
  }

  if (error) {
    return <ErrorMessage message={error} />;
  }

  return (
    <div className="view-applications-container">
      <div className="view-applications-header">
        <h2>Applications for Job ID: {jobId}</h2>
        <ErrorMessage message={error} />
        <div className="applications-summary">
          <p><strong>Total Applications:</strong> {totalCount}</p>
          <div className="filter-top">
            <label>Show Top </label>
            <input
              type="number"
              min="0"
              value={topN === 0 ? '' : topN}
              onChange={handleFilterTopN}
              placeholder="All"
              className="top-n-input"
            />
            <label>Candidates (Ranked by Score)</label>
          </div>
        </div>
      </div>

      {filteredApplications.length > 0 ? (
        <div className="applications-grid">
          {filteredApplications.map((app) => (
            <div key={app.id} className="application-card">
              <div
                className="application-header"
                onClick={() => toggleDetails(app.id)}
              >
                <div className="candidate-info">
                  <h3 className="candidate-name">{app.candidate?.name || 'N/A'}</h3>
                  <p className="candidate-location">{app.candidate?.location || 'N/A'}</p>
                </div>
                <div className="application-score">
                  <span className="score-label">Overall Score</span>
                  <span className="score-value">{app.overallScore.toFixed(1)}</span>
                </div>
              </div>

              {expandedAppId === app.id && (
                <div className="application-details">
                  <div className="details-grid">
                    <div className="resume-section">
                      <h4>Resume Preview</h4>
                      <ResumePreview resumeUrl={app.resumeUrl} />
                    </div>
                    <div className="ai-section">
                      <AIDataView aiData={app.aiData} />
                      <div className="status-selector">
                        <label>Application Status:</label>
                        <select
                          value={app.status}
                          onChange={(e) => handleStatusUpdate(app.id, e.target.value)}
                          className="status-dropdown"
                        >
                          <option value="Final Review">Final Review</option>
                          <option value="Shortlisted">Shortlisted</option>
                          <option value="Rejected">Rejected</option>
                        </select>
                      </div>
                    </div>
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      ) : (
        <div className="no-applications">
          <p>No applications found for this job.</p>
        </div>
      )}
    </div>
  );
};

export default ViewApplication;