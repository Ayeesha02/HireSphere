package hiringSystem.model;

import jakarta.persistence.*;

@Entity
@Table(name = "interview_responses")
public class InterviewResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "applicationId", referencedColumnName = "id")
    private Applications application;

    @Column(name = "question", length = 1000)
    private String question;
    @Column(name = "candidate_response", length = 1000)
    private String candidateResponse;
    private int aiScore; // AI-generated response score (0-100)
    private String questionType; // "technical" or "behavioral"

    public InterviewResponse() {
    }

    public InterviewResponse(Applications application, String question, String candidateResponse, int aiScore,
            String questionType) {

        this.application = application;
        this.question = question;
        this.candidateResponse = candidateResponse;
        this.aiScore = aiScore;
        this.questionType = questionType;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public Applications getApplication() {
        return application;
    }

    public void setApplication(Applications application) {
        this.application = application;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getCandidateResponse() {
        return candidateResponse;
    }

    public void setCandidateResponse(String candidateResponse) {
        this.candidateResponse = candidateResponse;
    }

    public int getAiScore() {
        return aiScore;
    }

    public void setAiScore(int aiScore) {
        this.aiScore = aiScore;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }
}