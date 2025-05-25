/**
 * CandidateService.java
 *  This service class handles candidate-related operations 
 */

package hiringSystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import hiringSystem.model.*;
import hiringSystem.repository.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.ByteArrayResource;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CandidateService {

    @Autowired
    private UserRepository userProfileRepository;

    @Autowired
    private JobInfoRepository jobInfoRepository;

    @Autowired
    private ApplicationRepository applicationsRepository;

    @Autowired
    private AIDataRepository aiDataRepository;

    @Autowired
    private JobScoreRepository jobScoreRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private InterviewResponseRepository interviewResponseRepository;

    @Autowired
    private BiasRepository biasRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private RestTemplate restTemplate; // For API calls

    /**
     * Register a new user with a specific role (e.g., candidate, recruiter)
     * 
     * @param email    email address of the user
     * @param password password for the user
     * @param role     role of the user (e.g., candidate, recruiter)
     */
    public UserRole registerUser(String email, String password, String role) {
        if (userRoleRepository.findByEmail(email) != null) {
            throw new RuntimeException("Email is already registered");
        }

        String encryptedPassword = new BCryptPasswordEncoder().encode(password);

        UserRole userRole = new UserRole(email, encryptedPassword, role);
        return userRoleRepository.save(userRole);
    }

    /**
     * Register a candidate with consent
     * 
     * @param candidate    UserProfile object containing candidate details
     * @param email        email address of the candidate
     * @param password     password for the candidate
     * @param consentGiven boolean indicating if the candidate has given consent
     * @return
     */
    public UserProfile registerCandidate(UserProfile candidate, String email, String password, boolean consentGiven) {
        if (userRoleRepository.findByEmail(email) != null) {
            throw new RuntimeException("Email is already registered.");
        }
        if (!consentGiven) {
            throw new RuntimeException("Consent is required for data processing.");
        }

        UserRole userRole = new UserRole(email, passwordEncoder.encode(password), "candidate");
        userRole.setConsentGiven(true);
        userRole.setConsentDate(new Date());
        userRole = userRoleRepository.save(userRole);
        candidate.setUser(userRole);
        UserProfile savedCandidate = userProfileRepository.save(candidate);

        auditLogRepository.save(new AuditLog(
                email,
                "REGISTER",
                new Date(),
                "Candidate registered with consent on," + userRole.getConsentDate() + " created at "
                        + userRole.getCreatedAt(),
                userRole.getRole()));

        return savedCandidate;
    }

    /**
     * Get current candidate
     * 
     * @return UserProfile object of the current candidate
     */
    private UserProfile getCurrentCandidate() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("No authenticated candidate found");
        }
        String email = auth.getName();
        UserRole userRole = userRoleRepository.findByEmail(email);
        if (userRole == null || !"candidate".equalsIgnoreCase(userRole.getRole())) {
            throw new RuntimeException("Authenticated user is not a candidate");
        }
        return userProfileRepository.findByUser(userRole)
                .orElseThrow(() -> new RuntimeException("Candidate profile not found for email: " + email));
    }

    /**
     * View Job Details of a specific job
     * 
     * @param jobId ID of the job
     * @return Optional containing JobInfo object if found, otherwise empty
     */

    public Optional<JobInfo> getJobDetails(Long jobId) {
        return jobInfoRepository.findById(jobId);
    }

    /**
     * Fetch candidate profile by userId
     * 
     * @return Optional containing UserProfile object if found, otherwise empty
     */
    public Optional<UserProfile> getCandidateProfile() {
        return userProfileRepository.findById(getCurrentCandidate().getUserId());
    }

    /**
     * Update candidate profile
     * 
     * @param updatedProfile UserProfile object containing updated details
     * @return updated UserProfile object
     */
    public UserProfile updateProfile(UserProfile updatedProfile) {
        UserProfile candidate = getCurrentCandidate();
        UserRole userRole = candidate.getUser();
        candidate.setName(updatedProfile.getName());
        candidate.setLocation(updatedProfile.getLocation());
        candidate.setEducationLevel(updatedProfile.getEducationLevel());
        candidate.setYearsOfExperience(updatedProfile.getYearsOfExperience());
        candidate.setPreferredJobTypes(updatedProfile.getPreferredJobTypes());
        candidate.setAvailability(updatedProfile.getAvailability());
        candidate.setPreviousCompanies(updatedProfile.getPreviousCompanies());
        candidate.setSkills(updatedProfile.getSkills());
        candidate.setGender(updatedProfile.getGender());
        candidate.setAge(updatedProfile.getAge());
        UserProfile savedCandidate = userProfileRepository.save(candidate);
        userRoleRepository.save(userRole);
        auditLogRepository.save(new AuditLog(
                userRole.getEmail(),
                "PROFILE_UPDATE",
                new Date(),
                "Candidate profile updated at " + userRole.getUpdatedAt(),
                userRole.getRole()));
        return savedCandidate;
    }

    /**
     * Search & filter jobs
     * 
     * @param keyword     search keyword
     * @param location    job location
     * @param workType    type of work (e.g., full-time, part-time)
     * @param salaryRange salary range
     * @return List of JobInfo objects matching the search criteria
     * 
     */
    public List<JobInfo> searchJobs(String keyword, String location, String workType, String salaryRange) {
        return jobInfoRepository.findAll().stream()
                .filter(job -> (keyword == null || job.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                        || (location == null || job.getCity().equalsIgnoreCase(location))
                        || (workType == null || job.getWorkType().equalsIgnoreCase(workType))
                        || (salaryRange == null || job.getSalaryRange().equalsIgnoreCase(salaryRange)))
                .collect(Collectors.toList());
    }

    /**
     * Get all jobs
     * 
     * @return List of all JobInfo objects
     **/
    public List<JobInfo> getAllJobs() {
        return jobInfoRepository.findAll();

    }

    /**
     * Apply for a job
     * 
     * @param jobId      ID of the job
     * @param resumeFile MultipartFile containing the resume
     * @return Applications object containing application details
     */
    public Applications applyForJob(Long jobId, MultipartFile resumeFile) {
        UserProfile candidate = getCurrentCandidate();
        JobInfo job = jobInfoRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        byte[] resumeBytes;
        try {
            resumeBytes = resumeFile.getBytes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to process resume file", e);
        }

        String originalFilename = resumeFile.getOriginalFilename();
        if (originalFilename == null) {
            throw new RuntimeException("Resume file name is null");
        }
        String fileType = null;
        if (originalFilename.endsWith(".pdf")) {
            fileType = "pdf";
        } else if (originalFilename.endsWith(".docx")) {
            fileType = "docx";
        } else if (originalFilename.endsWith(".txt")) {
            fileType = "txt";
        } else {
            throw new RuntimeException("Unsupported file type: " + originalFilename);
        }

        Applications application = new Applications();
        application.setCandidate(candidate);
        application.setJob(job);
        application.setresumeByte(resumeBytes);
        application.setStatus("Under Review");
        application.setApplicationDate(new Date());

        Applications savedApplication = applicationsRepository.save(application);
        AiData aiData = performAIScreening(savedApplication, fileType);
        aiDataRepository.save(aiData);

        int matchScore = calculateJobMatchScore(candidate, job);
        JobScore jobScore = new JobScore();
        jobScore.setCandidate(candidate);
        jobScore.setJob(job);
        jobScore.setMatchScore(matchScore);
        jobScoreRepository.save(jobScore);

        return savedApplication;
    }

    /**
     * Get interview responses for a specific application
     * 
     * @param applicationId ID of the application
     * @return List of InterviewResponse objects
     */
    public List<InterviewResponse> getInterviewResponses(Long applicationId) {
        return interviewResponseRepository.findByApplicationId(applicationId);
    }

    /**
     * Calculate personality score based on interview responses
     * 
     * @param application Application object
     * @return calculated personality score
     */
    private int calculateInterviewScore(Applications application) {
        List<InterviewResponse> responses = interviewResponseRepository.findByApplicationId(application.getId());

        return (int) responses.stream()
                .filter(r -> "behavioral".equals(r.getQuestionType()) || "technical".equals(r.getQuestionType()))
                .mapToInt(InterviewResponse::getAiScore)
                .average()
                .orElse(0);
    }

    /**
     * Perform AI screening on the application
     * 
     * @param application Application object
     * @param fileType    type of the resume file
     * @return AiData object containing AI-generated scores and feedback
     **/
    private AiData performAIScreening(Applications application, String fileType) {
        AiData aiData = new AiData();
        aiData.setApplication(application);

        int resumeScore = callResumeScreeningAPI(
                application.getresumeByte(),
                fileType,
                application.getJob());
        aiData.setResumeScore(resumeScore);

        int interviewScore = calculateInterviewScore(application);
        aiData.setInterviewScore(interviewScore);

        int skillMatchScore = callSkillMatchingAPI(application.getCandidate(), application.getJob());
        aiData.setSkillMatchScore(skillMatchScore);

        String biasDetectionResult = callBiasDetectionAPI(application);
        aiData.setBiasDetectionResult(biasDetectionResult);

        int personalityScore = calculatePersonalityScore(application);
        aiData.setPersonalityScore(personalityScore);

        int overallScore = calculateOverallScore(aiData);
        aiData.setOverallScore(overallScore);

        aiData.setAiFeedback(generateAIFeedback(aiData));
        return aiData;
    }

    public Optional<Bias> getBiasForApplication(Long applicationId) {
        return biasRepository.findByApplicationId(applicationId);
    }

    /**
     * Call external resume screening API
     * 
     * @param resumeBytes byte array of the resume
     * @param fileType    type of the resume file
     * @param job         JobInfo object containing job details
     * @return resume score
     */
    private int callResumeScreeningAPI(byte[] resumeBytes, String fileType, JobInfo job) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("resume", new ByteArrayResource(resumeBytes) {
            @Override
            public String getFilename() {
                return "resume." + fileType;
            }
        });
        List<String> requiredSkills = job.getRequiredSkills();
        if (requiredSkills != null) {
            for (String skill : requiredSkills) {
                body.add("required_skills[]", skill);
            }
        }
        List<String> preferredQualifications = job.getPreferredQualifications();
        if (preferredQualifications != null) {
            for (String qualification : preferredQualifications) {
                body.add("preferred_qualifications[]", qualification);
            }
        }
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:5001/screen_resume",
                HttpMethod.POST,
                requestEntity,
                Map.class);
        Number resumeScore = (Number) response.getBody().get("resume_score");
        return resumeScore != null ? resumeScore.intValue() : 0; // Default to 0 if null
    }

    /**
     * Generate detailed AI feedback
     * 
     * @param aiData AiData object containing AI-generated scores
     * @return formatted feedback string
     */
    private String generateAIFeedback(AiData aiData) {
        Map<String, Object> resumeScreeningDetails = callResumeScreeningAPIDetails(aiData.getApplication());

        // Safely convert potential Double values to Integer
        Number confidence = (Number) resumeScreeningDetails.get("confidence");
        Number relevanceScore = (Number) resumeScreeningDetails.get("relevance_score");

        int confidenceInt = confidence != null ? confidence.intValue() : 0;
        int relevanceScoreInt = relevanceScore != null ? relevanceScore.intValue() : 0;

        return String.format(
                "----- AI Evaluation Summary---\n" +
                        "Resume Score     : %d/100\n" +
                        " - Confidence    : %d%%\n" +
                        " - Relevance     : %d%%\n" +
                        "Behavioral Score : %d/100\n" +
                        "Skills Score     : %d/100\n" +
                        "Matched Skills   : %s",
                aiData.getResumeScore(),
                confidenceInt,
                relevanceScoreInt,
                aiData.getInterviewScore(),
                aiData.getSkillMatchScore(),
                resumeScreeningDetails.get("matched_skills"));
    }

    /**
     * Helper method to get full resume screening details
     * 
     * @param application Applications object
     * @return Map containing detailed resume screening results
     **/
    private Map<String, Object> callResumeScreeningAPIDetails(Applications application) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("resume", new ByteArrayResource(application.getresumeByte()) {
            @Override
            public String getFilename() {
                return "resume.pdf";
            }
        });
        body.add("required_skills[]", application.getJob().getRequiredSkills());
        body.add("preferred_qualifications[]", application.getJob().getPreferredQualifications());

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:5001/screen_resume",
                HttpMethod.POST,
                requestEntity,
                Map.class);
        return response.getBody();
    }

    /**
     * Call external skill matching API
     * 
     * @param candidate UserProfile object containing candidate details
     * @param job       JobInfo object containing job details
     * @return skill match score
     */
    private int callSkillMatchingAPI(UserProfile candidate, JobInfo job) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("candidate_skills", candidate.getSkills());
        requestBody.put("job_skills", job.getRequiredSkills());

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "http://localhost:5001/match_skills", // Updated endpoint
                    requestBody,
                    Map.class);

            return (int) response.getBody().get("skill_match_score");
        } catch (Exception e) {
            throw new RuntimeException("Failed to call skill matching API: " + e.getMessage(), e);
        }
    }

    /**
     * Call external bias detection API
     * 
     * @param application Applications object containing application details
     * 
     * @return bias detection result
     */
    private String callBiasDetectionAPI(Applications application) {
        UserProfile candidate = application.getCandidate();
        JobInfo job = application.getJob();

        Map<String, Object> candidateData = new HashMap<>();
        candidateData.put("Gender", candidate.getGender());
        candidateData.put("Age", candidate.getAge());
        candidateData.put("ExperienceYears", candidate.getYearsOfExperience());
        candidateData.put("EducationLevel", mapEducationLevel(candidate.getEducationLevel()));
        candidateData.put("DistanceFromCompany", calculateLocationScore(candidate, job));
        candidateData.put("PersonalityScore", calculatePersonalityScore(application));
        candidateData.put("SkillScore", calculateSkillScore(candidate, job));
        candidateData.put("HiringDecision", application.getStatus().equals("Shortlisted") ? 1 : 0);
        candidateData.put("InterviewScore", calculateInterviewScore(application));
        candidateData.put("PreviousCompanies", candidate.getPreviousCompanies());
        candidateData.put("RecruitmentStrategy", job.getRecruitmentStrategy());

        if (candidate.getGender() == null || candidate.getGender().trim().isEmpty()) {
            candidate.setGender("Unknown");
        }
        Map<String, Object> request = Map.of("candidate_data", candidateData);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:5002/analyze_bias", request, String.class);
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(response.getBody());

            double biasScoreGender = jsonNode.get("bias_score_gender").asDouble();
            double biasScoreAge = jsonNode.get("bias_score_age").asDouble();
            int predictedDecision = jsonNode.get("predicted_decision").asInt();
            boolean biasDetected = jsonNode.get("bias_detected").asBoolean();
            Optional<Bias> existingBias = biasRepository.findByApplicationId(application.getId());
            Bias bias;
            if (existingBias.isPresent()) {
                bias = existingBias.get();
                bias.setBiasScoreGender(biasScoreGender);
                bias.setBiasScoreAge(biasScoreAge);
                bias.setPredictedDecision(predictedDecision);
                bias.setBiasDetected(biasDetected);
            } else {
                bias = new Bias(application, biasScoreGender, biasScoreAge, predictedDecision, biasDetected);
            }
            biasRepository.save(bias);

            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse bias detection API response: " + e.getMessage(), e);
        }
    }

    /**
     * Get bias detection results for a specific application
     * 
     * @param applicationId
     * @return
     */
    public Optional<Bias> getBiasDetectionResults(Long applicationId) {
        return biasRepository.findByApplicationId(applicationId);
    }

    /**
     * Get candidate applications status
     * 
     * @return List of Applications objects
     *
     */
    public List<Applications> getCandidateApplications() {
        UserProfile candidate = getCurrentCandidate();
        return applicationsRepository.findByCandidate_UserId(candidate.getUser().getId());
    }

    /**
     * Get job recommendations
     * 
     * @return List of recommended JobInfo objects
     **/
    public List<JobInfo> getJobRecommendations() {
        UserProfile candidate = getCurrentCandidate();
        List<JobInfo> allJobs = jobInfoRepository.findAll();
        List<JobScore> existingScores = jobScoreRepository.findByCandidate(candidate);
        Map<JobInfo, Integer> jobScores = new HashMap<>();
        for (JobScore score : existingScores) {
            jobScores.put(score.getJob(), score.getMatchScore());
        }
        for (JobInfo job : allJobs) {
            if (!jobScores.containsKey(job)) {
                int matchScore = calculateJobMatchScore(candidate, job);
                jobScores.put(job, matchScore);
                JobScore jobScore = new JobScore();
                jobScore.setCandidate(candidate);
                jobScore.setJob(job);
                jobScore.setMatchScore(matchScore);
                jobScoreRepository.save(jobScore);
            }
        }

        return jobScores.entrySet().stream()
                .sorted(Map.Entry.<JobInfo, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * Calculate job match score based on skills, experience, and preferences
     * 
     * @param candidate UserProfile object containing candidate details
     * @param job       JobInfo object containing job details
     * @return calculated match score
     */
    private int calculateJobMatchScore(UserProfile candidate, JobInfo job) {
        int matchScore = 0;

        // Skill matching (40% weight)
        Set<String> candidateSkills = new HashSet<>(candidate.getSkills());
        Set<String> requiredSkills = new HashSet<>(job.getRequiredSkills());
        Set<String> skillIntersection = new HashSet<>(candidateSkills);
        skillIntersection.retainAll(requiredSkills);
        double skillMatch = requiredSkills.isEmpty() ? 0 : (double) skillIntersection.size() / requiredSkills.size();
        matchScore += (int) (skillMatch * 40);

        // Experience matching (30% weight)
        double experienceMatch = Math.min(candidate.getYearsOfExperience() / 5.0, 1.0);
        matchScore += (int) (experienceMatch * 30);

        // Location matching (20% weight)
        int locationScore = calculateLocationScore(candidate, job);
        matchScore += (int) (locationScore * 0.20);

        // Education matching (10% weight)
        double educationBonus = getEducationBonus(candidate.getEducationLevel());
        matchScore += (int) (educationBonus * 10);

        return Math.clamp(matchScore, 0, 100);
    }

    @Transactional
    public String startInterview(Long applicationId) {
        return getNextQuestion(applicationId);
    }

    /**
     * Get the next interview question for the chatbot interview
     * 
     * @param applicationId ID of the application
     * @return the next interview question
     */
    @Transactional
    public String getNextQuestion(Long applicationId) {
        Applications application = applicationsRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        List<InterviewResponse> responses = interviewResponseRepository.findByApplicationId(applicationId);

        long technicalCount = responses.stream().filter(r -> "technical".equals(r.getQuestionType())).count();
        long behavioralCount = responses.stream().filter(r -> "behavioral".equals(r.getQuestionType())).count();
        System.out.println("Technical count: " + technicalCount + ", Behavioral count: " + behavioralCount);

        JobInfo job = application.getJob();
        Map<String, Object> questionRequest = new HashMap<>();
        questionRequest.put("job_title", job.getTitle());
        questionRequest.put("required_skills", job.getRequiredSkills());
        questionRequest.put("preferred_qualifications", job.getPreferredQualifications());
        questionRequest.put("technical_count", technicalCount);
        questionRequest.put("behavioral_count", behavioralCount);

        ResponseEntity<Map> questionResponse = restTemplate.postForEntity(
                "http://localhost:5004/generate_question", questionRequest, Map.class);

        Map<String, Object> responseBody = questionResponse.getBody();
        System.out.println("Flask API response: " + responseBody);

        if (responseBody.containsKey("message") && "Interview completed".equals(responseBody.get("message"))) {
            System.out.println("Returning 'Interview completed.'");
            return "Interview completed.";
        }

        String question = (String) responseBody.get("question");
        if (question == null) {
            throw new RuntimeException("No question returned from Flask API");
        }

        String questionType = (String) responseBody.get("question_type");
        InterviewResponse interviewResponse = new InterviewResponse();
        interviewResponse.setApplication(application);
        interviewResponse.setQuestion(question);
        interviewResponse.setQuestionType(questionType);
        interviewResponse.setCandidateResponse(null);
        interviewResponse.setAiScore(0);
        InterviewResponse savedResponse = interviewResponseRepository.save(interviewResponse);
        interviewResponseRepository.flush(); // Ensure DB sync
        System.out.println("Saved response: " + savedResponse);

        return question;
    }

    /**
     * Submit and evaluate the candidate's response to an interview question
     * 
     * @param applicationId     ID of the application
     * @param candidateResponse candidate's response
     * @return InterviewResponse object containing the saved response
     */
    public InterviewResponse submitResponse(Long applicationId, String candidateResponse) {
        InterviewResponse lastResponse = interviewResponseRepository
                .findTopByApplication_IdAndCandidateResponseIsNullOrderByIdDesc(applicationId)
                .orElseThrow(() -> new RuntimeException("No pending question found"));

        Map<String, String> evaluationRequest = Map.of(
                "question", lastResponse.getQuestion(),
                "response", candidateResponse,
                "question_type", lastResponse.getQuestionType());

        ResponseEntity<Map> evaluationResponse = restTemplate.postForEntity(
                "http://localhost:5004/conduct_interview",
                evaluationRequest,
                Map.class);

        Map<String, Object> responseBody = evaluationResponse.getBody();

        lastResponse.setCandidateResponse(candidateResponse); // update
        lastResponse.setAiScore((int) Math.round((Double) responseBody.get("overall_score")));

        return interviewResponseRepository.save(lastResponse);
    }

    /**
     * Calculate skill score based on candidate's skills and job requirements
     * 
     * @param candidate UserProfile object containing candidate details
     * @param job       JobInfo object containing job details
     * @return calculated skill score
     */
    private int calculateSkillScore(UserProfile candidate, JobInfo job) {
        Set<String> candidateSkills = new HashSet<>(candidate.getSkills());
        Set<String> requiredSkills = new HashSet<>(job.getRequiredSkills());

        // Basic skill match
        Set<String> intersection = new HashSet<>(candidateSkills);
        intersection.retainAll(requiredSkills);
        double skillMatch = (double) intersection.size() / requiredSkills.size();

        // Experience bonus
        double experienceBonus = Math.min(candidate.getYearsOfExperience() / 5.0, 1.0);

        // Education bonus
        double educationBonus = getEducationBonus(candidate.getEducationLevel());

        return (int) ((skillMatch * 0.6 + experienceBonus * 0.25 + educationBonus * 0.15) * 100);
    }

    /**
     * Get education bonus based on education level
     * 
     * @param educationLevel education level of the candidate
     * @return calculated education bonus
     */
    private double getEducationBonus(String educationLevel) {
        return switch (educationLevel) {
            case "PhD" -> 1.0;
            case "Master's" -> 0.8;
            case "Bachelor's" -> 0.6;
            default -> 0.4;
        };
    }

    /**
     * Complete the interview process for a candidate
     * 
     * @param applicationId ID of the application
     * @return updated Applications object
     */
    public void completeInterview(Long applicationId) {
        Applications app = applicationsRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        List<InterviewResponse> responses = interviewResponseRepository.findByApplicationId(applicationId);
        if (responses.size() == 5) {
            app.setStatus("FinalReview");
            applicationsRepository.save(app);

            AiData aiData = aiDataRepository.findByApplicationId(applicationId)
                    .orElseThrow(() -> new RuntimeException("AiData not found for application"));

            int interviewScore = calculateInterviewScore(app);
            aiData.setInterviewScore(interviewScore);

            int personalityScore = calculatePersonalityScore(app);
            aiData.setPersonalityScore(personalityScore);

            aiData.setOverallScore(calculateOverallScore(aiData));

            aiData.setAiFeedback(generateAIFeedback(aiData));
            aiDataRepository.save(aiData);

            // Re-run bias analysis with updated data
            callBiasDetectionAPI(app);
            if (aiData.getOverallScore() < 50) {
                app.setStatus("Rejected");
            } else {
                app.setStatus("Shortlisted");
            }
        } else {
            throw new RuntimeException(
                    "Interview not fully completed. Expected 5 questions, found " + responses.size());
        }
    }

    /**
     * Calculate overall score based on various factors
     * 
     * @param aiData AiData object containing AI-generated scores
     * @return calculated overall score
     */
    private int calculateOverallScore(AiData aiData) {
        // Weighted Overall Score: 30% Resume + 30% Behavioral + 30% Skills + 10%
        // personlaity
        return (int) Math.round(
                (aiData.getResumeScore() * 0.3) +
                        (aiData.getInterviewScore() * 0.3) +
                        (aiData.getSkillMatchScore() * 0.3) +
                        (aiData.getPersonalityScore() * 0.1));
    }

    /**
     * Calculate personality score based on interview responses
     * 
     * @param application Applications object
     * @return calculated personality score
     */
    private int calculatePersonalityScore(Applications application) {
        List<InterviewResponse> responses = interviewResponseRepository
                .findByApplicationId(application.getId());

        if (responses.isEmpty()) {
            return 70; // Base score
        }

        // Calculate weighted average of behavioral scores
        return (int) responses.stream()
                .filter(r -> "behavioral".equals(r.getQuestionType()))
                .mapToInt(InterviewResponse::getAiScore)
                .average()
                .orElse(70);
    }

    /**
     * Calculate location score based on candidate's location and job location
     * 
     * @param candidate UserProfile object containing candidate details
     * @param job       JobInfo object containing job details
     * @return calculated location score
     */
    public int calculateLocationScore(UserProfile candidate, JobInfo job) {
        if (candidate.isInSameMetroArea(job)) {
            return 100; // Full score for same metro area
        }

        // Partial score for remote candidates based on work preference
        return switch (candidate.getPreferredJobTypes().toLowerCase()) {
            case "full-time" -> 80;
            case "remote" -> 60;
            case "part-time" -> 60;
            case "contract" -> 70;
            case "hybrid" -> 70;
            default -> 40; // Prefers onsite but applying to different location
        };
    }

    /**
     * Map education level to a numerical value
     * 
     * @param educationLevel education level of the candidate
     * @return mapped numerical value
     */
    private int mapEducationLevel(String educationLevel) {
        switch (educationLevel) {
            case "Bachelor's":
                return 2;
            case "Master's":
                return 3;
            case "PhD":
                return 4;
            default:
                return 1;// Assume 1 is "High School" or baseline
        }
    }

    /**
     * Get AI data for a specific application
     * 
     * @param applicationId ID of the application
     * @return Optional containing AiData object if found, otherwise empty
     */
    public Optional<AiData> getAIDataForApplication(Long applicationId) {
        return aiDataRepository.findByApplicationId(applicationId);
    }

    /**
     * Get all data for current candidate (GDPR Right to Access)
     * 
     * @return Map containing user role, candidate profile, and applications
     */
    public Map<String, Object> getAllData() {
        UserProfile candidate = getCurrentCandidate();
        UserRole userRole = candidate.getUser();
        List<Applications> applications = applicationsRepository.findByCandidate_UserId(userRole.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("userRole", userRole);
        data.put("candidateProfile", candidate);
        data.put("applications", applications);
        auditLogRepository.save(new AuditLog(
                userRole.getEmail(),
                "DATA_ACCESS",
                new Date(),
                "Candidate accessed all personal data" + userRole.getCreatedAt() +
                        ", last updated at " + userRole.getUpdatedAt(),
                userRole.getRole()));
        return data;
    }

    /**
     * Delete all data for current candidate (GDPR Right to Erasure)
     */
    public void deleteAllData() {
        UserProfile candidate = getCurrentCandidate();
        UserRole userRole = candidate.getUser();
        String email = userRole.getEmail();
        String role = userRole.getRole();
        auditLogRepository.save(new AuditLog(
                email,
                "DATA_DELETE",
                new Date(),
                "Candidate requested complete data deletion at " + new Date() + ", originally created at "
                        + userRole.getCreatedAt(),
                role));
        List<Applications> applications = applicationsRepository.findByCandidate_UserId(userRole.getId());
        applicationsRepository.deleteAll(applications);
        userProfileRepository.delete(candidate);
        userRoleRepository.delete(userRole);
    }

    /**
     * Get current candidate's profile
     * 
     * @return UserProfile object containing candidate details
     */
    public List<JobScore> getJobScoresForCandidate() {
        UserProfile candidate = getCurrentCandidate();
        return jobScoreRepository.findByCandidate(candidate);
    }
}
