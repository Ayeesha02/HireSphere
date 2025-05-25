package hiringSystem.model;

import jakarta.persistence.*;

@Entity
@Table(name = "job_match_scores")
public class JobScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "candidateId", referencedColumnName = "user_id")
    private UserProfile candidate;

    @ManyToOne
    @JoinColumn(name = "jobId", referencedColumnName = "id")
    private JobInfo job;

    private int matchScore;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserProfile getCandidate() {
        return candidate;
    }

    public void setCandidate(UserProfile candidate) {
        this.candidate = candidate;
    }

    public JobInfo getJob() {
        return job;
    }

    public void setJob(JobInfo job) {
        this.job = job;
    }

    public int getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(int matchScore) {
        this.matchScore = matchScore;
    }
}