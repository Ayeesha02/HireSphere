/**
 * JobScoreRepository.java
 * This interface extends JpaRepository to provide CRUD operations for the JobScore entity.
 */
package hiringSystem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import hiringSystem.model.JobInfo;
import hiringSystem.model.JobScore;
import hiringSystem.model.UserProfile;

@Repository
public interface JobScoreRepository extends JpaRepository<JobScore, Long> {
    List<JobScore> findByCandidate(UserProfile candidate);

    Optional<JobScore> findByCandidateAndJob(UserProfile candidate, JobInfo job);
}
