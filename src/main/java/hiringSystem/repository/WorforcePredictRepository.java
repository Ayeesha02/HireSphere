/**
 * WorforcePredictRepository.java
 * This interface extends JpaRepository to provide CRUD operations for the WorkforcePrediction entity.
 */

package hiringSystem.repository;

import hiringSystem.model.WorkforcePrediction;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorforcePredictRepository extends JpaRepository<WorkforcePrediction, Long> {
    List<WorkforcePrediction> findByRecruiter_UserId(Long recruiterId);
}
