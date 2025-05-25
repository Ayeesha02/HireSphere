/**
 * ApplicationRepository.java
 * This interface extends JpaRepository to provide CRUD operations for the Applications entity.
 */
package hiringSystem.repository;

import hiringSystem.model.Applications;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends JpaRepository<Applications, Long> {
    List<Applications> findByJobId(Long jobId);

    List<Applications> findByCandidate_UserId(Long candidateId);

    List<Applications> findByJob_RecruiterUserId(Long recruiterId);

    long countByJobId(Long jobId);

}
