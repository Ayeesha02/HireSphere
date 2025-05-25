/**
 * RecruiterController.java
 * This class handles HTTP requests related to recruiter operations
 */

package hiringSystem.controller;

import hiringSystem.model.*;
import hiringSystem.repository.ApplicationRepository;
import hiringSystem.service.RecruiterService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import hiringSystem.security.UserDetailService;
import hiringSystem.security.JwtUtil;

@RestController
@RequestMapping("/recruiters")
public class RecruiterController {

    @Autowired
    private RecruiterService recruiterService;

    @Autowired
    private ApplicationRepository applicationsRepository;

    @Autowired
    private UserDetailService userDetailsService;
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * Register a new recruiter
     * 
     * @param request contains email, password, and consentGiven
     * @return ResponseEntity with the saved recruiter details or an error message
     */
    @PostMapping("/register/recruiter")
    public ResponseEntity<?> registerRecruiter(@RequestBody Map<String, Object> request) {
        try {
            String email = (String) request.get("email");
            String password = (String) request.get("password");
            Boolean consentGiven = (Boolean) request.get("consentGiven");
            if (consentGiven == null || !consentGiven) {
                throw new RuntimeException("Consent is required.");
            }

            RecruiterProfile recruiter = new RecruiterProfile();
            recruiter.setCompanyName((String) request.get("companyName"));
            recruiter.setCompanySize((Integer) request.get("companySize"));
            recruiter.setIndustry((String) request.get("industry"));
            recruiter.setLocation((String) request.get("location"));
            recruiter.setCompanyWebsite((String) request.get("companyWebsite"));
            recruiter.setContactEmail((String) request.get("contactEmail"));
            recruiter.setCompanyDescription((String) request.get("companyDescription"));
            recruiter.setHiringPreferences((String) request.get("hiringPreferences"));

            RecruiterProfile savedRecruiter = recruiterService.registerRecruiter(recruiter, email, password,
                    consentGiven);
            return ResponseEntity.ok(savedRecruiter);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "An unexpected error occurred."));
        }
    }

    /**
     * Login and issue JWT
     * 
     * @param credentials contains email and password
     * @return ResponseEntity with the generated JWT token or an error message
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");
        System.out.println("Login attempt: email=" + email + ", password=" + password);
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            String role = userDetails.getAuthorities().stream()
                    .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                    .findFirst().orElse("recruiter");
            String token = jwtUtil.generateToken(email, role);
            System.out.println("Token generated: " + token);
            return ResponseEntity.ok(Map.of("token", token));
        } catch (Exception e) {
            System.out.println("Authentication failed: " + e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
    }

    /**
     * View current recruiter profile
     * 
     * @return ResponseEntity with the recruiter profile
     * @throws NotFoundException if the recruiter profile is not found
     **/
    @GetMapping("/me")
    public ResponseEntity<RecruiterProfile> getMyProfile() {
        RecruiterProfile recruiter = recruiterService.getCurrentRecruiter();
        return ResponseEntity.ok(recruiter);
    }

    /**
     * Update recruiter profile
     * 
     * @param updatedProfile contains updated recruiter profile details
     * @return ResponseEntity with the updated recruiter profile
     **/
    @PutMapping("/update-me")
    public ResponseEntity<RecruiterProfile> updateRecruiterProfile(
            @RequestBody RecruiterProfile updatedProfile) {
        RecruiterProfile updatedRecruiter = recruiterService.updateRecruiterProfile(updatedProfile);
        return ResponseEntity.ok(updatedRecruiter);
    }

    /**
     * Post a new job
     * 
     * @param job contains job details
     * @return ResponseEntity with the saved job details
     */
    @PostMapping("/jobs")
    public ResponseEntity<JobInfo> postJob(@RequestBody JobInfo job) {
        JobInfo savedJob = recruiterService.postJob(job);
        return ResponseEntity.ok(savedJob);
    }

    /**
     * View all job postings for a recruiter
     * 
     * @return ResponseEntity with the list of job postings
     */
    @GetMapping("/jobs")
    public ResponseEntity<List<JobInfo>> getAllJobPostings() {
        List<JobInfo> jobs = recruiterService.getAllJobPostings();
        return ResponseEntity.ok(jobs);
    }

    /**
     * View job applications for a specific job
     * 
     * @param jobId the ID of the job
     * @return ResponseEntity with the list of applications for the job
     **/
    @GetMapping("/jobs/{jobId}/applications")
    public ResponseEntity<List<Applications>> getJobApplications(@PathVariable Long jobId) {
        List<Applications> applications = recruiterService.getJobApplications(jobId);
        return ResponseEntity.ok(applications);
    }

    /**
     * Get AI data for a specific application
     * 
     * @param applicationId the ID of the application
     * @return ResponseEntity with the AI data for the application
     */
    @GetMapping("/applications/{applicationId}/ai-data")
    public ResponseEntity<AiData> getAIDataForApplication(@PathVariable Long applicationId) {
        Optional<AiData> aiData = recruiterService.getAIDataForApplication(applicationId);
        return aiData.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get all applications for the current recruiter
     * 
     * @return ResponseEntity with the list of applications
     */
    @GetMapping("/applications")
    public ResponseEntity<List<Applications>> getApplicationsByRecruiter() {
        List<Applications> applications = recruiterService.getApplicationsByRecruiter();
        return ResponseEntity.ok(applications);
    }

    /**
     * Get bias detection results for a specific application
     * 
     * @param applicationId the ID of the application
     * @return ResponseEntity with the bias detection results and AI scores
     */
    @GetMapping("/applications/{applicationId}/bias")
    public ResponseEntity<Map<String, Object>> getBiasDetectionResults(@PathVariable Long applicationId) {
        Bias bias = recruiterService.getBiasDetectionResults(applicationId)
                .orElseThrow(() -> new RuntimeException("Bias data not found"));
        AiData aiData = recruiterService.getAIDataForApplication(applicationId)
                .orElseThrow(() -> new RuntimeException("AI data not found"));

        return ResponseEntity.ok(Map.of(
                "biasAnalysis", bias,
                "aiScores", Map.of(
                        "resumeScore", aiData.getResumeScore(),
                        "skillScore", aiData.getSkillMatchScore(),
                        "personalityScore", aiData.getPersonalityScore())));
    }

    /**
     * Update the status of a specific application
     * 
     * @param applicationId the ID of the application
     * @return ResponseEntity with the updated application details
     */
    @PutMapping("/applications/{applicationId}/status")
    public ResponseEntity<Applications> updateApplicationStatus(
            @PathVariable Long applicationId, @RequestBody Map<String, String> requestBody) throws NotFoundException {
        String status = requestBody.get("status");
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }
        Applications application = applicationsRepository.findById(applicationId)
                .orElseThrow(NotFoundException::new);
        application.setStatus(status);
        return ResponseEntity.ok(applicationsRepository.save(application));
    }

    /**
     * Endpoint to generate and retrieve dashboard metrics for the current
     * recruiter.
     * 
     * @param period the period for which to generate metrics (e.g., "daily",
     *               "weekly", "monthly")
     * @return ResponseEntity with the generated dashboard metrics or an error
     *         message
     */
    @PostMapping("/dashboard/generate-and-get")
    public ResponseEntity<?> generateAndGetDashboardMetrics(@RequestParam String period) {
        try {
            recruiterService.generateDashboardMetrics(period); // Generate metrics
            List<Dashboard> metrics = recruiterService.getDashboardMetrics(period); // Fetch generated metrics
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Failed to generate and retrieve dashboard metrics: " + e.getMessage());
        }
    }

    /**
     * Endpoint to generate a prediction based on the uploaded CSV file.
     * 
     * @param csvFile the CSV file containing the data for prediction
     * @return ResponseEntity with the prediction results
     */
    @PostMapping("/predict")
    public ResponseEntity<Map<String, Object>> predict(@RequestParam("file") MultipartFile csvFile) {
        WorkforcePrediction prediction = recruiterService.generatePrediction(csvFile);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> response = new HashMap<>();
        response.put("predicted_hires", prediction.getPredictedHires());
        response.put("predicted_turnover", prediction.getPredictedTurnover());
        try {
            response.put("skills_demand", mapper.readValue(prediction.getPredictedSkillsDemand(), Map.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse skills demand: " + e.getMessage(), e);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Get all data (GDPR Right to Access)
     * 
     * @return ResponseEntity with all data
     */
    @GetMapping("/data")
    public ResponseEntity<Map<String, Object>> getAllData() {
        Map<String, Object> data = recruiterService.getAllData();
        return ResponseEntity.ok(data);
    }

    /**
     * Delete all data (GDPR Right to Erasure)
     * 
     * @return ResponseEntity with a success message
     */
    @DeleteMapping("/data")
    public ResponseEntity<String> deleteAllData() {
        recruiterService.deleteAllData();
        return ResponseEntity.ok("All data deleted successfully.");
    }

    /**
     * Endpoint to retrieve the total number of applications.
     * 
     * @return ResponseEntity containing the total count of applications
     */
    @GetMapping("/applications/count")
    public ResponseEntity<Long> getTotalApplications() {
        return ResponseEntity.ok(recruiterService.getTotalApplications());
    }

    /**
     * Endpoint to retrieve the number of applications for a specific job.
     * 
     * @param jobId the ID of the job
     * @return ResponseEntity containing the count of applications for the job
     */
    @GetMapping("/applications/count/{jobId}")
    public ResponseEntity<Long> getApplicationsForJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(recruiterService.getApplicationsForJob(jobId));
    }

    /**
     * Endpoint to retrieve the resume of a specific application.
     * 
     * @param applicationId the ID of the application
     * @return ResponseEntity containing the resume as a byte array resource
     */
    @GetMapping("/applications/{applicationId}/resume")
    public ResponseEntity<ByteArrayResource> getApplicationResume(@PathVariable Long applicationId) {
        Optional<Applications> applicationOpt = applicationsRepository.findById(applicationId);
        if (applicationOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Applications application = applicationOpt.get();
        byte[] resumeData = application.getresumeByte(); // Fixed getter name
        if (resumeData == null || resumeData.length == 0) {
            return ResponseEntity.noContent().build();
        }

        ByteArrayResource resource = new ByteArrayResource(resumeData);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=resume.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(resumeData.length)
                .body(resource);
    }
}
