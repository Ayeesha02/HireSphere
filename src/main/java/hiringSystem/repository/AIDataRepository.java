/**
 * AIDataRepository.java
 * This interface extends JpaRepository to provide CRUD operations for the AiData entity.
 */

package hiringSystem.repository;

import hiringSystem.model.AiData;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AIDataRepository extends JpaRepository<AiData, Long> {
    Optional<AiData> findByApplicationId(Long applicationId);

    List<AiData> findByApplication_Job_RecruiterUserId(Long recruiterId);
}