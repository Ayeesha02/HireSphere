package hiringSystem.model;

import java.util.*;

import jakarta.persistence.*;

@Entity
@Table(name = "workforce_predictions")
public class WorkforcePrediction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recruiterId", referencedColumnName = "user_id")
    private RecruiterProfile recruiter;

    private Date predictionDate;
    private int predictedHires;
    private int predictedTurnover;
    @Column(columnDefinition = "TEXT")
    private String predictedSkillsDemand; // JSON or comma-separated list

    public WorkforcePrediction() {

    }

    public WorkforcePrediction(Long id, RecruiterProfile recruiter, Date predictionDate, int predictedHires,
            int predictedTurnover, String predictedSkillsDemand) {
        this.id = id;
        this.recruiter = recruiter;
        this.predictionDate = predictionDate;
        this.predictedHires = predictedHires;
        this.predictedTurnover = predictedTurnover;
        this.predictedSkillsDemand = predictedSkillsDemand;
    }

    // Getters and Setters
    public Long getId() {
        return id;
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

    public Date getPredictionDate() {
        return predictionDate;
    }

    public void setPredictionDate(Date predictionDate) {
        this.predictionDate = predictionDate;
    }

    public int getPredictedHires() {
        return predictedHires;
    }

    public void setPredictedHires(int predictedHires) {
        this.predictedHires = predictedHires;
    }

    public int getPredictedTurnover() {
        return predictedTurnover;
    }

    public void setPredictedTurnover(int predictedTurnover) {
        this.predictedTurnover = predictedTurnover;
    }

    public String getPredictedSkillsDemand() {
        return predictedSkillsDemand;
    }

    public void setPredictedSkillsDemand(String predictedSkillsDemand) {
        this.predictedSkillsDemand = predictedSkillsDemand;
    }
}