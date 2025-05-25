package hiringSystem.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "dashboard_metrics")
public class Dashboard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recruiterId", referencedColumnName = "user_id")
    private RecruiterProfile recruiter;

    private int totalApplications;
    private int shortlistedCandidates;
    private Date metricDate;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public Dashboard(Long id, RecruiterProfile recruiter, int totalApplications, int shortlistedCandidates,
            Date metricDate) {
        this.id = id;
        this.recruiter = recruiter;
        this.totalApplications = totalApplications;
        this.shortlistedCandidates = shortlistedCandidates;
        this.metricDate = metricDate;
    }

    public Dashboard() {

    }

    public void setId(Long id) {
        this.id = id;
    }

    public RecruiterProfile getRecruiter() {
        return recruiter;
    }

    public void setRecruiter(RecruiterProfile recruiter) {
        this.recruiter = recruiter;
    }

    public int getTotalApplications() {
        return totalApplications;
    }

    public void setTotalApplications(int totalApplications) {
        this.totalApplications = totalApplications;
    }

    public int getShortlistedCandidates() {
        return shortlistedCandidates;
    }

    public void setShortlistedCandidates(int shortlistedCandidates) {
        this.shortlistedCandidates = shortlistedCandidates;
    }

    public Date getMetricDate() {
        return metricDate;
    }

    public void setMetricDate(Date metricDate) {
        this.metricDate = metricDate;
    }
}