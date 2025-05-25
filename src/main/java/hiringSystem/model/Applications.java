package hiringSystem.model;

import java.util.Date;

import jakarta.persistence.*;

@Entity
@Table(name = "applications")
public class Applications {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "jobId", referencedColumnName = "id")
    private JobInfo job;

    @ManyToOne
    @JoinColumn(name = "candidateId", referencedColumnName = "user_id")
    private UserProfile candidate;

    private byte[] resumeByte; // resumeURL string
    private String status = "Under Review"; // "Under Review", "Shortlisted", "Rejected"
    private Date applicationDate;
    private int aiScore; // AI-generated score for the candidate
    private String feedback; // feedback
    private Date hireDate;

    public Applications(JobInfo job, UserProfile candidate, byte[] resumeByte,
            String status, Date applicationDate, int aiScore, String feedback, Date hireDate) {
        this.job = job;
        this.candidate = candidate;
        this.resumeByte = resumeByte;
        this.status = status;
        this.applicationDate = applicationDate;
        this.aiScore = aiScore;
        this.feedback = feedback;
        this.hireDate = hireDate;
    }

    public Applications() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public JobInfo getJob() {
        return job;
    }

    public void setJob(JobInfo job) {
        this.job = job;
    }

    public UserProfile getCandidate() {
        return candidate;
    }

    public void setCandidate(UserProfile candidate) {
        this.candidate = candidate;
    }

    public byte[] getresumeByte() {
        return resumeByte;
    }

    public void setresumeByte(byte[] resumeBytes) {
        this.resumeByte = resumeBytes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(Date applicationDate) {
        this.applicationDate = applicationDate;
    }

    public int getAiScore() {
        return aiScore;
    }

    public void setAiScore(int aiScore) {
        this.aiScore = aiScore;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public Date getHireDate() {
        return hireDate;
    }

    public void setHireDate(Date hireDate) {
        this.hireDate = hireDate;
    }
}