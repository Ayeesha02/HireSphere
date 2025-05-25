package hiringSystem.model;

import jakarta.persistence.*;

@Entity
@Table(name = "bias_detection")
public class Bias {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "applicationId", referencedColumnName = "id")
    private Applications application;

    @Column(name = "bias_score_gender")
    private double biasScoreGender; // Bias score for gender

    @Column(name = "bias_score_age")
    private double biasScoreAge;
    private int predictedDecision; // Model’s hiring prediction (0 or 1)

    private boolean biasDetected; // True if biasScore exceeds a threshold

    public Bias() {
    }

    public Bias(Applications application, double biasScoreGender, double biasScoreAge, int predictedDecision,
            boolean biasDetected) {
        this.application = application;
        this.biasScoreGender = biasScoreGender;
        this.biasScoreAge = biasScoreAge;

        this.predictedDecision = predictedDecision;
        this.biasDetected = biasDetected;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Applications getApplication() {
        return application;
    }

    public void setApplication(Applications application) {
        this.application = application;
    }

    public double getBiasScoreGender() {
        return biasScoreGender;
    }

    public void setBiasScoreGender(double biasScoreGender) {
        this.biasScoreGender = biasScoreGender;
    }

    public double getBiasScoreAge() {
        return biasScoreAge;
    }

    public void setBiasScoreAge(double biasScoreAge) {
        this.biasScoreAge = biasScoreAge;
    }

    public int getPredictedDecision() {
        return predictedDecision;
    }

    public void setPredictedDecision(int predictedDecision) {
        this.predictedDecision = predictedDecision;
    }

    public boolean isBiasDetected() {
        return biasDetected;
    }

    public void setBiasDetected(boolean biasDetected) {
        this.biasDetected = biasDetected;
    }
}