package hiringSystem.model;

import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "user_profiles")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    private String name;
    private String location; // city, state
    private String educationLevel;
    private int yearsOfExperience;
    private String preferredJobTypes; // JSON or comma-separated list
    private String availability; // e.g., "Immediate", "2 weeks notice"
    private int previousCompanies;
    private int age;

    @ElementCollection
    private List<String> skills;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserRole user;

    @Column(nullable = false)
    private String gender = "Unknown"; // Default to "Unknown" if not provided

    public UserProfile() {
    }

    public UserProfile(String name, String location, String educationLevel,
            int yearsOfExperience, String preferredJobTypes, String availability, List<String> skills,
            UserRole user, String gender, int previousCompanies, int age) {
        this.age = age;
        this.name = name;
        this.location = location;
        this.educationLevel = educationLevel;
        this.yearsOfExperience = yearsOfExperience;
        this.preferredJobTypes = preferredJobTypes;
        this.availability = availability;
        this.skills = skills;
        this.previousCompanies = previousCompanies;
        this.user = user;
        this.gender = (gender == null || gender.isEmpty()) ? "Unknown" : gender;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getEducationLevel() {
        return educationLevel;
    }

    public void setEducationLevel(String educationLevel) {
        this.educationLevel = educationLevel;
    }

    public int getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(int yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public String getPreferredJobTypes() {
        return preferredJobTypes;
    }

    public void setPreferredJobTypes(String preferredJobTypes) {
        this.preferredJobTypes = preferredJobTypes;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public UserRole getUser() {
        return user;
    }

    public void setUser(UserRole user) {
        this.user = user;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = (gender == null || gender.isEmpty()) ? "Unknown" : gender;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getPreviousCompanies() {
        return previousCompanies;
    }

    public void setPreviousCompanies(int previousCompanies) {
        this.previousCompanies = previousCompanies;
    }

    public boolean isInSameMetroArea(JobInfo job) {
        return this.location.split(",")[0].equalsIgnoreCase(
                job.getLocation().split(",")[0]);

    }
}