package hiringSystem.model;

import jakarta.persistence.*;

@Entity
@Table(name = "recruiter_profiles")
public class RecruiterProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    private String companyName;
    private int companySize;
    private String industry;
    private String location;
    private String companyWebsite;
    private String contactEmail;
    private String companyDescription;
    private String hiringPreferences; // JSON or comma-separated list of preferences

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserRole user;

    public RecruiterProfile(String companyName, int companySize, String industry, String location,
            String companyWebsite, String contactEmail, String companyDescription, String hiringPreferences,
            UserRole user) {
        this.companyName = companyName;
        this.companySize = companySize;
        this.industry = industry;
        this.location = location;
        this.companyWebsite = companyWebsite;
        this.contactEmail = contactEmail;
        this.companyDescription = companyDescription;
        this.hiringPreferences = hiringPreferences;
        this.user = user;
    }

    public RecruiterProfile() {

    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public int getCompanySize() {
        return companySize;
    }

    public void setCompanySize(int companySize) {
        this.companySize = companySize;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCompanyWebsite() {
        return companyWebsite;
    }

    public void setCompanyWebsite(String companyWebsite) {
        this.companyWebsite = companyWebsite;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getCompanyDescription() {
        return companyDescription;
    }

    public void setCompanyDescription(String companyDescription) {
        this.companyDescription = companyDescription;
    }

    public String getHiringPreferences() {
        return hiringPreferences;
    }

    public void setHiringPreferences(String hiringPreferences) {
        this.hiringPreferences = hiringPreferences;
    }

    public UserRole getUser() {
        return user;
    }

    public void setUser(UserRole user) {
        this.user = user;
    }
}
