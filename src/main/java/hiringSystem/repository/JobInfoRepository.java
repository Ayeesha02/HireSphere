/**
 * JobInfoRepository.java
 * This interface extends JpaRepository to provide CRUD operations for the JobInfo entity.
 */
package hiringSystem.repository;

import hiringSystem.model.JobInfo;
import hiringSystem.model.RecruiterProfile;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobInfoRepository extends JpaRepository<JobInfo, Long> {

    List<JobInfo> findByRecruiterUserId(Long recruiterId);

    Optional<JobInfo> findByIdAndRecruiter(Long id, RecruiterProfile recruiter);

    List<JobInfo> findByRequiredSkillsContainingAndLocationAndWorkTypeAndSalaryRange(
            String skill, String location, String workType, String salaryRange);
}