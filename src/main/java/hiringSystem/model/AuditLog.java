package hiringSystem.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false) // Prevent updates
    private String email;

    @Column(nullable = false, updatable = false)
    private String action;

    @Column(nullable = false, updatable = false)
    private Date timestamp;

    @Column(updatable = false)
    private String details;

    @Column(updatable = false)
    private String role;

    public AuditLog() {
    }

    public AuditLog(String email, String action, Date timestamp, String details, String role) {
        this.email = email;
        this.action = action;
        this.timestamp = timestamp;
        this.details = details;
        this.role = role;
    }

    // Getters only (no setters for immutability)
    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getAction() {
        return action;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getDetails() {
        return details;
    }

    public String getRole() {
        return role;
    }
}