package hiringSystem.controller;

import hiringSystem.model.*;
import hiringSystem.security.JwtUtil;
import hiringSystem.security.UserDetailService;
import hiringSystem.service.CandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for candidate-related endpoints.
 * This class handles all candidate-related operations such as registration,
 * login, job search, application management, and interview processes.
 */
@RestController
@RequestMapping("/candidates")
public class CandidateController {

    @Autowired
    private CandidateService candidateService;
    @Autowired
    private UserDetailService userDetailsService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * Register a new candidate.
     * 
     * @param request contains candidate details such as name, email, password, etc.
     * @return ResponseEntity with the saved candidate details or an error message
     */
    @PostMapping("/register")
    public ResponseEntity<UserProfile> registerCandidate(@RequestBody Map<String, Object> request) {
        try {
            String email = (String) request.get("email");
            String password = (String) request.get("password");
            Boolean consentGiven = (Boolean) request.get("consentGiven");
            if (consentGiven == null || !consentGiven) {
                throw new RuntimeException("Consent is required.");
            }

            UserProfile candidate = new UserProfile();
            candidate.setName((String) request.get("name"));
            candidate.setLocation((String) request.get("location"));
            candidate.setEducationLevel((String) request.get("educationLevel"));
            candidate.setYearsOfExperience((Integer) request.get("yearsOfExperience"));
            candidate.setPreviousCompanies((Integer) request.get("previousCompanies"));
            candidate.setPreferredJobTypes((String) request.get("preferredJobTypes"));
            candidate.setAvailability((String) request.get("availability"));
            candidate.setSkills((List<String>) request.get("skills"));
            candidate.setGender((String) request.get("gender"));
            candidate.setAge((Integer) request.get("age"));

            UserProfile savedCandidate = candidateService.registerCandidate(candidate, email, password, consentGiven);
            return ResponseEntity.ok(savedCandidate);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Login a candidate and generate a JWT token.
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
                    .findFirst().orElse("candidate");
            String token = jwtUtil.generateToken(email, role);
            System.out.println("Token generated: " + token);
            return ResponseEntity.ok(Map.of("token", token));
        } catch (Exception e) {
            System.out.println("Authentication failed: " + e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
    }

    /**
     * Fetch Job details for particular Job ID
     * 
     * @param jobId the ID of the job
     * @return ResponseEntity with the job details or an error message
     */
    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<JobInfo> getJobDetails(@PathVariable Long jobId) {
        Optional<JobInfo> job = candidateService.getJobDetails(jobId);
        return job.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Fetch candidate profile
     * 
     * @return ResponseEntity with the candidate profile or an error message
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfile> getCandidateProfile() {
        Optional<UserProfile> candidate = candidateService.getCandidateProfile();
        return candidate.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Update candidate profile
     * 
     * @param updatedProfile contains the updated candidate profile details
     * @return ResponseEntity with the updated candidate profile or an error message
     */
    @PutMapping("/update-me")
    public ResponseEntity<UserProfile> updateProfile(@RequestBody UserProfile updatedProfile) {
        UserProfile updatedCandidate = candidateService.updateProfile(updatedProfile);
        return ResponseEntity.ok(updatedCandidate);
    }

    /**
     * Search & filter jobs
     * 
     * @param keyword     the keyword to search for
     * @param location    the location to filter by
     * @param workType    the type of work (e.g., full-time, part-time)
     * @param salaryRange the salary range to filter by
     * @return ResponseEntity with the list of jobs matching the criteria or an
     *         error message
     */
    @GetMapping("/jobs/search")
    public ResponseEntity<List<JobInfo>> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String workType,
            @RequestParam(required = false) String salaryRange) {
        List<JobInfo> jobs = candidateService.searchJobs(keyword, location, workType, salaryRange);
        return ResponseEntity.ok(jobs);
    }

    /**
     * Get all jobs
     * 
     * @return ResponseEntity with the list of all jobs or an error message
     */
    @GetMapping("/All-jobs")
    public ResponseEntity<List<JobInfo>> getAllJobs() {
        List<JobInfo> jobs = candidateService.getAllJobs();
        return ResponseEntity.ok(jobs);
    }

    /**
     * Apply for a job (Upload Resume & AI Screening)
     * 
     * @param jobId      the ID of the job to apply for
     * @param resumeFile the resume file to upload
     * @return ResponseEntity with the application details and AI analysis or an
     *         error message
     */
    @PostMapping("/apply/{jobId}")
    public ResponseEntity<Map<String, Object>> applyForJob(
            @PathVariable Long jobId,
            @RequestParam("resume") MultipartFile resumeFile) {
        Applications application = candidateService.applyForJob(jobId, resumeFile);
        AiData aiData = candidateService.getAIDataForApplication(application.getId())
                .orElseThrow(() -> new RuntimeException("AI data not found"));

        return ResponseEntity.ok(Map.of(
                "application", application,
                "aiAnalysis", aiData));
    }

    /**
     * Track application status
     * 
     * @param applicationId the ID of the application
     * @return ResponseEntity with the application status or an error message
     */
    @GetMapping("/applications")
    public ResponseEntity<List<Applications>> getCandidateApplications() {
        List<Applications> applications = candidateService.getCandidateApplications();
        return ResponseEntity.ok(applications);
    }

    /**
     * job recommendations for the candidate
     * 
     * @return ResponseEntity with the list of recommended jobs or an error message
     */
    @GetMapping("/job-recommendations")
    public ResponseEntity<List<JobInfo>> getJobRecommendations() {
        List<JobInfo> recommendations = candidateService.getJobRecommendations();
        return ResponseEntity.ok(recommendations);
    }

    /**
     * Start a new interview session
     * 
     * @param applicationId the ID of the application
     * @return ResponseEntity with the first question or an error message
     */
    @PostMapping("/start")
    public ResponseEntity<String> startInterview(@RequestParam Long applicationId) {
        try {
            String firstQuestion = candidateService.getNextQuestion(applicationId);
            return ResponseEntity.ok(firstQuestion);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to start interview: " + e.getMessage());
        }
    }

    /**
     * Get the next question in the interview
     * 
     * @param applicationId the ID of the application
     * @return ResponseEntity with the next question or an error message
     */
    @GetMapping("/{applicationId}/next-question")
    public ResponseEntity<String> getNextQuestion(@PathVariable Long applicationId) {
        try {
            String question = candidateService.getNextQuestion(applicationId);
            return ResponseEntity.ok(question);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Submit candidate's response
     * 
     * @param applicationId the ID of the application
     * @param request       contains the response to be submitted
     * @return ResponseEntity with the saved response or an error message
     */
    @PostMapping("/{applicationId}/submit")
    public ResponseEntity<?> submitResponse(
            @PathVariable Long applicationId,
            @RequestBody Map<String, String> request) {
        try {
            String response = request.get("response");
            InterviewResponse savedResponse = candidateService.submitResponse(applicationId, response);
            return ResponseEntity.ok(savedResponse);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to submit response: " + e.getMessage()));
        }
    }

    /**
     * Get interview results
     * 
     * @param applicationId the ID of the application
     * @return ResponseEntity with the list of interview responses or an error
     *         message
     */
    @GetMapping("/{applicationId}/results")
    public ResponseEntity<List<InterviewResponse>> getResults(@PathVariable Long applicationId) {
        List<InterviewResponse> responses = candidateService.getInterviewResponses(applicationId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get AI data for an application
     * 
     * @param applicationId the ID of the application
     * @return ResponseEntity with the AI data or an error message
     */
    @GetMapping("/applications/{applicationId}/ai-data")
    public ResponseEntity<AiData> getAIDataForApplication(@PathVariable Long applicationId) {
        return candidateService.getAIDataForApplication(applicationId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get interview responses for an application
     * 
     * @param applicationId the ID of the application
     * @return ResponseEntity with the list of interview responses or an error
     *         message
     */
    @GetMapping("/applications/{applicationId}/interview-responses")
    public ResponseEntity<List<InterviewResponse>> getInterviewResponses(@PathVariable Long applicationId) {
        return ResponseEntity.ok(candidateService.getInterviewResponses(applicationId));
    }

    /**
     * Get bias detection results for an application
     * 
     * @param applicationId the ID of the application
     * @return ResponseEntity with the bias detection results or an error message
     */
    @GetMapping("/applications/{applicationId}/bias")
    public ResponseEntity<Bias> getBiasForApplication(@PathVariable Long applicationId) {
        Optional<Bias> bias = candidateService.getBiasForApplication(applicationId);
        return bias.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Complete interview process
     * 
     * @param applicationId the ID of the application
     * @return ResponseEntity with a success message or an error message
     */
    @PostMapping("/applications/{applicationId}/complete-interview")
    public ResponseEntity<String> completeInterview(@PathVariable Long applicationId) {
        try {
            candidateService.completeInterview(applicationId);
            return ResponseEntity.ok("Interview completed successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to complete interview: " + e.getMessage());
        }
    }

    /**
     * Get all data (GDPR Right to Access)
     * 
     * @param email the email of the user
     * @return ResponseEntity with the user data or an error message
     */
    @GetMapping("/data")
    public ResponseEntity<Map<String, Object>> getAllData() {
        Map<String, Object> data = candidateService.getAllData();
        return ResponseEntity.ok(data);
    }

    /**
     * Delete all data (GDPR Right to Erasure)
     * 
     * @param email the email of the user
     * @return ResponseEntity with a success message or an error message
     */
    @DeleteMapping("/data")
    public ResponseEntity<String> deleteAllData() {
        candidateService.deleteAllData();
        return ResponseEntity.ok("All data deleted successfully.");
    }

    /**
     * Get job scores for the candidate
     * 
     * @return ResponseEntity with the list of job scores or an error message
     */
    @GetMapping("/job-scores")
    public ResponseEntity<List<JobScore>> getJobScores() {
        List<JobScore> scores = candidateService.getJobScoresForCandidate();
        return ResponseEntity.ok(scores);
    }
}