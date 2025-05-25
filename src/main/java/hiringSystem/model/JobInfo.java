package hiringSystem.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "jobs")
public class JobInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String location;
    private String salaryRange;
    private String workType;
    private String recruitmentStrategy;

    @Column(columnDefinition = "TEXT")
    private String requiredSkills; // Store as JSON string

    @Column(columnDefinition = "TEXT")
    private String preferredQualifications; // Store as JSON string

    private Date applicationDeadline;

    @ManyToOne
    @JoinColumn(name = "recruiterId", referencedColumnName = "user_id")
    private RecruiterProfile recruiter;

    public JobInfo() {
    }

    public JobInfo(String title, String description, String location, String salaryRange, String workType,
            List<String> requiredSkills, List<String> preferredQualifications, String recruitmentStrategy,
            Date applicationDeadline,
            RecruiterProfile recruiter) {

        this.title = title;
        this.description = description;
        this.location = location;
        this.salaryRange = salaryRange;
        this.workType = workType;
        this.requiredSkills = serializeList(requiredSkills); // Serialize to JSON
        this.preferredQualifications = serializeList(preferredQualifications); // Serialize to JSON
        this.applicationDeadline = applicationDeadline;
        this.recruitmentStrategy = recruitmentStrategy;
        this.recruiter = recruiter;
    }

    // Helper method to serialize a list to JSON
    private String serializeList(List<String> list) {
        if (list == null) {
            return "[]"; // Default to empty JSON array
        }
        try {
            return new ObjectMapper().writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize list to JSON", e);
        }
    }

    // Helper method to deserialize JSON to a list
    private List<String> deserializeList(String json) {
        if (json == null || json.isEmpty()) {
            return List.of(); // Default to empty list
        }
        try {
            return new ObjectMapper().readValue(json, List.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON to list", e);
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSalaryRange() {
        return salaryRange;
    }

    public void setSalaryRange(String salaryRange) {
        this.salaryRange = salaryRange;
    }

    public String getWorkType() {
        return workType;
    }

    public void setWorkType(String workType) {
        this.workType = workType;
    }

    @JsonIgnore // Ignore this field during JSON serialization
    public String getRequiredSkillsJson() {
        return requiredSkills;
    }

    public void setRequiredSkillsJson(String requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    @JsonIgnore // Ignore this field during JSON serialization
    public String getPreferredQualificationsJson() {
        return preferredQualifications;
    }

    public void setPreferredQualificationsJson(String preferredQualifications) {
        this.preferredQualifications = preferredQualifications;
    }

    // Custom getters and setters for deserialized fields
    public List<String> getRequiredSkills() {
        return deserializeList(requiredSkills);
    }

    public void setRequiredSkills(List<String> requiredSkills) {
        this.requiredSkills = serializeList(requiredSkills);
    }

    public List<String> getPreferredQualifications() {
        return deserializeList(preferredQualifications);
    }

    public void setPreferredQualifications(List<String> preferredQualifications) {
        this.preferredQualifications = serializeList(preferredQualifications);
    }

    public Date getApplicationDeadline() {
        return applicationDeadline;
    }

    public void setApplicationDeadline(Date applicationDeadline) {
        this.applicationDeadline = applicationDeadline;
    }

    public RecruiterProfile getRecruiter() {
        return recruiter;
    }

    public void setRecruiter(RecruiterProfile recruiter) {
        this.recruiter = recruiter;
    }

    public String getRecruitmentStrategy() {
        return recruitmentStrategy;
    }

    public void setRecruitmentStrategy(String recruitmentStrategy) {
        this.recruitmentStrategy = recruitmentStrategy;
    }

    public String getCity() {
        return location.split(",")[0].trim().toLowerCase();
    }
}