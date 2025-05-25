package hiringSystem.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ai_data")
public class AiData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int resumeScore;
    private int interviewScore; // behavioralScore (technical + behavioral)
    private int skillMatchScore;
    private String biasDetectionResult; // JSON or text describing bias detection
    private int overallScore;
    private String aiFeedback;
    private int personalityScore;

    public AiData(Applications application, int resumeScore, int interviewScore, int skillMatchScore,
            String biasDetectionResult, int overallScore, String aiFeedback, int personalityScore) {
        this.application = application;
        this.resumeScore = resumeScore;
        this.interviewScore = interviewScore;
        this.skillMatchScore = skillMatchScore;
        this.biasDetectionResult = biasDetectionResult;
        this.overallScore = overallScore;
        this.aiFeedback = aiFeedback;
        this.personalityScore = personalityScore;
    }

    public AiData() {

    }

    @OneToOne
    @JoinColumn(name = "applicationId", referencedColumnName = "id")
    private Applications application;

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

    public int getResumeScore() {
        return resumeScore;
    }

    public void setResumeScore(int resumeScore) {
        this.resumeScore = resumeScore;
    }

    public int getInterviewScore() {
        return interviewScore;
    }

    public void setInterviewScore(int interviewScore) {
        this.interviewScore = interviewScore;
    }

    public int getSkillMatchScore() {
        return skillMatchScore;
    }

    public void setSkillMatchScore(int skillMatchScore) {
        this.skillMatchScore = skillMatchScore;
    }

    public String getBiasDetectionResult() {
        return biasDetectionResult;
    }

    public void setBiasDetectionResult(String biasDetectionResult) {
        this.biasDetectionResult = biasDetectionResult;
    }

    public int getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(int overallScore) {
        this.overallScore = overallScore;
    }

    public String getAiFeedback() {
        return aiFeedback;
    }

    public void setAiFeedback(String aiFeedback) {
        this.aiFeedback = aiFeedback;
    }

    public int getPersonalityScore() {
        return personalityScore;
    }

    public void setPersonalityScore(int personalityScore) {
        this.personalityScore = personalityScore;
    }
}