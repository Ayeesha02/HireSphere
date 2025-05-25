/**
 * InterviewResponseRepository.java
 * This interface extends JpaRepository to provide CRUD operations for the InterviewResponse entity.
 */

package hiringSystem.repository;

import hiringSystem.model.InterviewResponse;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterviewResponseRepository extends JpaRepository<InterviewResponse, Long> {
    List<InterviewResponse> findByApplicationId(Long applicationId);

    Optional<InterviewResponse> findTopByApplication_IdAndCandidateResponseIsNullOrderByIdDesc(Long applicationId);

}
