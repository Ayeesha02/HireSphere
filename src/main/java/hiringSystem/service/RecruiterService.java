/**
 *  Recruiter service clas 
 *  This class provides various functionalities related to recruiter
 */

package hiringSystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import hiringSystem.model.*;

import hiringSystem.repository.*;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RecruiterService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecruiterRepository recruiterProfileRepository;

    @Autowired
    private JobInfoRepository jobInfoRepository;

    @Autowired
    private ApplicationRepository applicationsRepository;

    @Autowired
    private AIDataRepository aiDataRepository;

    @Autowired
    private WorforcePredictRepository workforcePredictionRepository;

    @Autowired
    private BiasRepository biasRepository;

    @Autowired
    private DashboardRepository dashboardRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RestTemplate restTemplate; // For API calls

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * To Regiseter new recruiter
     * 
     * @param recruiter    recruiter profile
     * @param email        recruiter's email
     * @param password     recruiter's password
     * @param consentGiven consent given by recruiter
     * @return saved recruiter profile
     * 
     */
    public RecruiterProfile registerRecruiter(RecruiterProfile recruiter, String email, String password,
            boolean consentGiven) {
        if (userRoleRepository.findByEmail(email) != null) {
            throw new IllegalArgumentException("Email is already registered.");
        }
        if (!consentGiven) {
            throw new IllegalArgumentException("Consent is required for registeration.");
        }

        UserRole userRole = new UserRole(email, passwordEncoder.encode(password), "recruiter");
        userRole.setConsentGiven(true);
        userRole.setConsentDate(new Date());
        userRole = userRoleRepository.save(userRole);
        recruiter.setUser(userRole);
        RecruiterProfile savedRecruiter = recruiterProfileRepository.save(recruiter);

        auditLogRepository.save(new AuditLog(
                email,
                "REGISTER",
                new Date(),
                "Recruiter registered with consent on " + userRole.getConsentDate() +
                        ", created at " + userRole.getCreatedAt(),
                userRole.getRole()));
        return savedRecruiter;
    }

    /**
     * To get current recruiter
     * 
     * @return current recruiter profile
     */
    public RecruiterProfile getCurrentRecruiter() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new RuntimeException("No authenticated recruiter found");
        }
        String email = auth.getName(); // Email is the principal (username)
        UserRole userRole = userRoleRepository.findByEmail(email);
        if (userRole == null) {
            throw new RuntimeException("Recruiter not found for email: " + email);
        }
        return recruiterProfileRepository.findByUser(userRole)
                .orElseThrow(() -> new RuntimeException("Recruiter profile not found for email: " + email));
    }

    /**
     * To get recruiter profile by id
     * 
     * @param recruiterId recruiter id
     * @return recruiter profile
     */
    public Optional<RecruiterProfile> getRecruiterProfile(Long recruiterId) {
        return recruiterProfileRepository.findById(recruiterId);
    }

    /**
     * To update recruiter profile
     * 
     * @param updatedProfile updated recruiter profile
     * @return saved recruiter profile
     */
    public RecruiterProfile updateRecruiterProfile(RecruiterProfile updatedProfile) {
        RecruiterProfile recruiter = getCurrentRecruiter();
        UserRole userRole = recruiter.getUser();
        recruiter.setCompanyName(updatedProfile.getCompanyName());
        recruiter.setCompanySize(updatedProfile.getCompanySize());
        recruiter.setIndustry(updatedProfile.getIndustry());
        recruiter.setLocation(updatedProfile.getLocation());
        recruiter.setCompanyWebsite(updatedProfile.getCompanyWebsite());
        recruiter.setContactEmail(updatedProfile.getContactEmail());
        recruiter.setCompanyDescription(updatedProfile.getCompanyDescription());
        recruiter.setHiringPreferences(updatedProfile.getHiringPreferences());
        RecruiterProfile savedRecruiter = recruiterProfileRepository.save(recruiter);

        userRoleRepository.save(userRole);

        auditLogRepository.save(new AuditLog(
                userRole.getEmail(),
                "PROFILE_UPDATE",
                new Date(),
                "Recruiter profile updated at " + userRole.getUpdatedAt(),
                userRole.getRole()));
        return savedRecruiter;
    }

    /**
     * To post a new job
     * 
     * @param job job information
     * @return saved job information
     */
    public JobInfo postJob(JobInfo job) {
        RecruiterProfile recruiter = getCurrentRecruiter();
        job.setRecruiter(recruiter);
        return jobInfoRepository.save(job);
    }

    /**
     * get all job postings for the current recruiter
     * 
     * @return list of job postings
     **/
    public List<JobInfo> getAllJobPostings() {
        RecruiterProfile recruiter = getCurrentRecruiter();
        return jobInfoRepository.findByRecruiterUserId(recruiter.getUser().getId());
    }

    /**
     * Get job applications for a specific job posting
     * 
     * @param jobId job id
     * @return list of applications for the job
     **/
    public List<Applications> getJobApplications(Long jobId) {
        RecruiterProfile recruiter = getCurrentRecruiter();
        jobInfoRepository.findByIdAndRecruiter(jobId, recruiter)
                .orElseThrow(() -> new RuntimeException("Job not found or not owned by recruiter"));
        return applicationsRepository.findByJobId(jobId);
    }

    /**
     * Get all applications for the current recruiter
     * 
     * @return list of applications
     */
    public List<Applications> getApplicationsByRecruiter() {
        RecruiterProfile recruiter = getCurrentRecruiter();
        return applicationsRepository.findByJob_RecruiterUserId(recruiter.getUser().getId());
    }

    /**
     * Generate workforce prediction based on CSV file
     * 
     * @param csvFile CSV file containing workforce data
     * @return generated workforce prediction
     **/
    public WorkforcePrediction generatePrediction(MultipartFile csvFile) {
        RecruiterProfile recruiter = getCurrentRecruiter();
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    "http://localhost:5000/predict",
                    HttpMethod.POST,
                    createMultipartRequest(csvFile),
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            Map<String, Object> body = response.getBody();
            if (body == null || !response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Prediction API failed: " + response.getStatusCode());
            }

            System.out.println("Prediction API Response: " + body); // Debug log

            Object hires = body.get("predicted_hires");
            Object turnover = body.get("predicted_turnover");
            Object skillsDemand = body.get("skills_demand");

            // Stricter validation
            if (hires == null || !(hires instanceof Number)) {
                throw new RuntimeException("Invalid API response: 'predicted_hires' is missing or not a number");
            }
            if (turnover == null || !(turnover instanceof Number)) {
                throw new RuntimeException("Invalid API response: 'predicted_turnover' is missing or not a number");
            }
            if (skillsDemand == null || !(skillsDemand instanceof Map)) {
                throw new RuntimeException("Invalid API response: 'skills_demand' is missing or not a map");
            }

            ObjectMapper mapper = new ObjectMapper();
            String skillsDemandJson = mapper.writeValueAsString(skillsDemand);

            WorkforcePrediction prediction = new WorkforcePrediction(
                    null,
                    recruiter,
                    new Date(),
                    ((Number) hires).intValue(),
                    ((Number) turnover).intValue(),
                    skillsDemandJson);

            return workforcePredictionRepository.save(prediction);
        } catch (IOException e) {
            throw new RuntimeException("Failed to process file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate prediction: " + e.getMessage(), e);
        }
    }

    /**
     * Create a multipart request for file upload
     * 
     * @param file the file to be uploaded
     * @return HttpEntity containing the multipart request
     */
    private HttpEntity<MultiValueMap<String, Object>> createMultipartRequest(MultipartFile file) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        try {
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename() != null ? file.getOriginalFilename() : "employee_data_upload.csv";
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + e.getMessage(), e);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        return new HttpEntity<>(body, headers);
    }

    /**
     * Get AI data for a specific application
     * 
     * @param applicationId application id
     * @return AI data for the application
     **/
    public Optional<AiData> getAIDataForApplication(Long applicationId) {
        return aiDataRepository.findByApplicationId(applicationId);
    }

    /**
     * Get bias detection results for a specific application
     * 
     * @param applicationId application id
     * @return bias detection results
     **/
    public Optional<Bias> getBiasDetectionResults(Long applicationId) {
        return biasRepository.findByApplicationId(applicationId);
    }

    /**
     * Get workforce predictions for the current recruiter
     * 
     * @return list of workforce predictions
     **/
    public List<WorkforcePrediction> getWorkforcePredictions() {
        RecruiterProfile recruiter = getCurrentRecruiter();
        // Rest of the method remains similar, but use recruiter directly
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("recruiter_id", recruiter.getUser().getId());
        requestBody.put("workforce_data", getRecentHiringData(recruiter.getUser().getId()));

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:5003/predict_workforce",
                HttpMethod.POST,
                requestEntity,
                Map.class);

        List<WorkforcePrediction> predictions = new ArrayList<>();
        List<Double> predictionValues = (List<Double>) response.getBody().get("predicted_workforce");

        for (Double value : predictionValues) {
            WorkforcePrediction prediction = new WorkforcePrediction();
            prediction.setRecruiter(recruiter);
            prediction.setPredictedHires(value.intValue());
            prediction.setPredictionDate(new Date());
            predictions.add(prediction);
        }
        return predictions;
    }

    /**
     * Get recent hiring data for the current recruiter
     * 
     * @param recruiterId recruiter id
     * @return list of recent hiring data
     * 
     */
    private List<Double> getRecentHiringData(Long recruiterId) {
        List<WorkforcePrediction> predictions = workforcePredictionRepository.findByRecruiter_UserId(recruiterId);
        if (predictions.isEmpty()) {
            return Arrays.asList(0.0);
        }
        return predictions.stream()
                .map(wp -> (double) wp.getPredictedHires())
                .collect(Collectors.toList());
    }

    /**
     * Get dashboard metrics for the current recruiter
     * 
     * @return list of dashboard metrics
     **/
    public List<Dashboard> getDashboardMetrics() {
        RecruiterProfile recruiter = getCurrentRecruiter();
        return dashboardRepository.findByRecruiterUserId(recruiter.getUser().getId());
    }

    /**
     * Generate dashboard metrics for the current recruiter
     * 
     * @param period the period for which to generate metrics (daily, weekly,
     *               monthly)
     */
    public void generateDashboardMetrics(String period) {
        RecruiterProfile recruiter = getCurrentRecruiter();
        Long recruiterId = recruiter.getUser().getId();
        Dashboard metrics;

        // Determine period start for uniqueness
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        switch (period.toLowerCase()) {
            case "daily":
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                break;
            case "weekly":
                cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                break;
            case "monthly":
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                break;
            default:
                throw new IllegalArgumentException("Invalid period: " + period);
        }
        Date periodStart = cal.getTime();

        // Check if metrics exist for this period
        Optional<Dashboard> existingMetrics = dashboardRepository.findByRecruiterUserIdAndMetricDate(recruiterId,
                periodStart);
        if (existingMetrics.isPresent()) {
            metrics = existingMetrics.get();
        } else {
            metrics = new Dashboard();
            metrics.setRecruiter(recruiter);
            metrics.setMetricDate(periodStart);
        }

        List<Applications> applications = applicationsRepository.findByJob_RecruiterUserId(recruiterId);
        metrics.setTotalApplications(applications.size());

        long shortlisted = applications.stream()
                .filter(app -> "Shortlisted".equals(app.getStatus()))
                .count();
        metrics.setShortlistedCandidates((int) shortlisted);

        dashboardRepository.save(metrics);

        auditLogRepository.save(new AuditLog(
                recruiter.getUser().getEmail(),
                "METRICS_GENERATED",
                new Date(),
                "Dashboard metrics generated/updated for " + period + " period on " + periodStart,
                "recruiter"));
    }

    /**
     * Get dashboard metrics for the current recruiter
     * 
     * @param period the period for which to get metrics (daily, weekly, monthly)
     * @return list of dashboard metrics
     */
    public List<Dashboard> getDashboardMetrics(String period) {
        RecruiterProfile recruiter = getCurrentRecruiter();
        Long recruiterId = recruiter.getUser().getId();
        if (period == null) {
            return dashboardRepository.findByRecruiterUserId(recruiterId);
        }
        switch (period.toLowerCase()) {
            case "daily":
                return dashboardRepository.findDailyMetricsByRecruiterUserId(recruiterId);
            case "weekly":
                return dashboardRepository.findWeeklyMetricsByRecruiterUserId(recruiterId);
            case "monthly":
                return dashboardRepository.findMonthlyMetricsByRecruiterUserId(recruiterId);
            default:
                throw new IllegalArgumentException("Invalid period. Supported values: daily, weekly, monthly");
        }
    }

    /**
     * Get all data for current recruiter (GDPR Right to Access)
     * 
     * @return containing user role and recruiter profile
     */
    public Map<String, Object> getAllData() {
        RecruiterProfile recruiter = getCurrentRecruiter();
        UserRole userRole = recruiter.getUser();
        Map<String, Object> data = new HashMap<>();
        data.put("userRole", userRole);
        data.put("recruiterProfile", recruiter);
        auditLogRepository.save(new AuditLog(
                userRole.getEmail(),
                "DATA_ACCESS",
                new Date(),
                "Recruiter accessed all personal data, created at," + userRole.getCreatedAt() + " last updated at "
                        + userRole.getUpdatedAt(),
                userRole.getRole()));
        return data;
    }

    /**
     * To delete All data for current recruiter (GDPR Right to Erasure
     * 
     * @return void
     **/
    public void deleteAllData() {
        RecruiterProfile recruiter = getCurrentRecruiter();
        UserRole userRole = recruiter.getUser();
        String email = userRole.getEmail();
        String role = userRole.getRole();
        auditLogRepository.save(new AuditLog(
                email,
                "DATA_DELETE",
                new Date(),
                "Recruiter requested complete data deletion at " + new Date() +
                        ", originally created at " + userRole.getCreatedAt(),
                role));
        recruiterProfileRepository.delete(recruiter);
        userRoleRepository.delete(userRole);
    }

    @Scheduled(cron = "0 0 0 * * ?") // Daily at midnight
    public void autoGenerateDailyMetrics() {
        List<RecruiterProfile> recruiters = recruiterProfileRepository.findAll();
        for (RecruiterProfile recruiter : recruiters) {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(recruiter.getUser().getEmail(), null,
                            Collections.emptyList()));
            generateDashboardMetrics("daily");
        }
    }

    @Scheduled(cron = "0 0 0 * * MON") // Weekly on Monday at midnight
    public void autoGenerateWeeklyMetrics() {
        List<RecruiterProfile> recruiters = recruiterProfileRepository.findAll();
        for (RecruiterProfile recruiter : recruiters) {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(recruiter.getUser().getEmail(), null,
                            Collections.emptyList()));
            generateDashboardMetrics("weekly");
        }
    }

    @Scheduled(cron = "0 0 0 1 * ?") // Monthly on the 1st at midnight
    public void autoGenerateMonthlyMetrics() {
        List<RecruiterProfile> recruiters = recruiterProfileRepository.findAll();
        for (RecruiterProfile recruiter : recruiters) {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(recruiter.getUser().getEmail(), null,
                            Collections.emptyList()));
            generateDashboardMetrics("monthly");
        }
    }

    
    /**
     * Get total number of applications
     * 
     * @return total number of applications
     */
    public long getTotalApplications() {
        return applicationsRepository.count();
    }

    /**
     * Get total number of applications for a specific job
     * 
     * @param jobId job id
     * @return total number of applications for the job
     */
    public long getApplicationsForJob(Long jobId) {
        return applicationsRepository.countByJobId(jobId);
    }

}