package hiringSystem.controller;

// Import necessary packages
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import hiringSystem.model.AuditLog;
import hiringSystem.model.UserRole;
import hiringSystem.security.JwtUtil;
import hiringSystem.service.AdminService;

import java.util.List;
import java.util.Map;

/**
 * REST controller for Admin-related endpoints.
 * This class handles all admin-related operations such as registration, login,
 * data access, and audit log management.
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Register a new admin.
     * 
     * @param request contains email, password, and consentGiven
     * @return ResponseEntity with the saved admin details or an error message
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerAdmin(@RequestBody Map<String, Object> request) {
        try {
            String email = (String) request.get("email");
            String password = (String) request.get("password");
            Boolean consentGiven = (Boolean) request.get("consentGiven");
            if (consentGiven == null || !consentGiven) {
                throw new IllegalArgumentException("Consent is required.");
            }

            UserRole savedAdmin = adminService.registerAdmin(email, password, consentGiven);
            return ResponseEntity.ok(savedAdmin);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "An unexpected error occurred."));
        }
    }

    /**
     * Login an admin and generate a JWT token.
     * 
     * @param credentials contains email and password
     * @return ResponseEntity with the generated JWT token or an error message
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            String role = userDetails.getAuthorities().stream()
                    .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No role found for user"));
            if (!"admin".equalsIgnoreCase(role)) {
                throw new IllegalArgumentException("User is not an admin.");
            }
            String token = jwtUtil.generateToken(email, role);
            return ResponseEntity.ok(Map.of("token", token));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials or not an admin"));
        }
    }

    /**
     * Get all data for current admin.
     * 
     * @return ResponseEntity with the data or an error message
     */
    @GetMapping("/data")
    public ResponseEntity<Map<String, Object>> getAllData() {
        Map<String, Object> data = adminService.getAllData();
        return ResponseEntity.ok(data);
    }

    /**
     * Delete all data for current admin.
     * 
     * @return ResponseEntity with a success message or an error message
     */
    @DeleteMapping("/data")
    public ResponseEntity<String> deleteAllData() {
        adminService.deleteAllData();
        return ResponseEntity.ok("All data deleted successfully.");
    }

    /**
     * Get all audit logs.
     * 
     * @return ResponseEntity with the list of audit logs or an error message
     */
    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAllAuditLogs() {
        List<AuditLog> logs = adminService.getAllAuditLogs();
        return ResponseEntity.ok(logs);
    }

    /**
     * Get audit logs by email.
     * 
     * @param email the email of the user whose audit logs are to be fetched
     * @return ResponseEntity with the list of audit logs for the specified email or
     *         an error message
     */
    @GetMapping("/audit-logs/{email}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByEmail(@PathVariable String email) {
        List<AuditLog> logs = adminService.getAuditLogsByEmail(email);
        return ResponseEntity.ok(logs);
    }
}
