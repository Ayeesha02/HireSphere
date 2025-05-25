/**
 * AdminService.java
 * This class handles the admin functionalities in the hiring system.
 */

package hiringSystem.service;

import hiringSystem.model.AuditLog;
import hiringSystem.model.UserRole;
import hiringSystem.repository.AuditLogRepository;
import hiringSystem.repository.UserRoleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Register a new admin
     * 
     * @param email        admin email
     * @param password     password
     * @param consentGiven true if consent is given
     * @return UserRole
     */
    public UserRole registerAdmin(String email, String password, boolean consentGiven) {
        if (userRoleRepository.findByEmail(email) != null) {
            throw new IllegalArgumentException("Email is already registered.");
        }
        if (!consentGiven) {
            throw new IllegalArgumentException("Consent is required for data processing.");
        }

        UserRole userRole = new UserRole(email, passwordEncoder.encode(password), "admin");
        userRole.setConsentGiven(true);
        userRole.setConsentDate(new Date());
        userRole = userRoleRepository.save(userRole);

        auditLogRepository.save(new AuditLog(
                email,
                "REGISTER",
                new Date(),
                "Admin registered with consent on " + userRole.getConsentDate(),
                userRole.getRole()));

        return userRole;
    }

    /**
     * Get current admin
     * 
     * @return UserRole
     */
    private UserRole getCurrentAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("No authenticated admin found");
        }
        String email = auth.getName();
        UserRole userRole = userRoleRepository.findByEmail(email);
        if (userRole == null || !"admin".equalsIgnoreCase(userRole.getRole())) {
            throw new RuntimeException("Authenticated user is not an admin");
        }
        return userRole;
    }

    /**
     * Get all data for current admin (GDPR Right to Access)
     * 
     * @return Map<String, Object> containing all data
     */
    public Map<String, Object> getAllData() {
        UserRole admin = getCurrentAdmin();
        Map<String, Object> data = new HashMap<>();
        data.put("userRole", admin);
        auditLogRepository.save(new AuditLog(
                admin.getEmail(),
                "DATA_ACCESS",
                new Date(),
                "Admin accessed all personal data",
                admin.getRole()));
        return data;
    }

    /**
     * Delete all data for current admin (GDPR Right to Erasure)
     * 
     */
    public void deleteAllData() {
        UserRole admin = getCurrentAdmin();
        String email = admin.getEmail();
        String role = admin.getRole();
        auditLogRepository.save(new AuditLog(
                email,
                "DATA_DELETE",
                new Date(),
                "Admin requested complete data deletion at " + new Date(),
                role));
        userRoleRepository.delete(admin);
    }

    /**
     * Get all audit logs (for admin)
     */
    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }

    /** Get audit logs by email (for admin) **/
    public List<AuditLog> getAuditLogsByEmail(String email) {
        return auditLogRepository.findByEmail(email);
    }
}
