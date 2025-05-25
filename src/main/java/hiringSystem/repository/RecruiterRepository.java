/**
 * RecruiterRepository.java
 * This interface extends JpaRepository to provide CRUD operations for the RecruiterProfile entity.
 */
package hiringSystem.repository;

import hiringSystem.model.RecruiterProfile;
import hiringSystem.model.UserRole;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecruiterRepository extends JpaRepository<RecruiterProfile, Long> {
    RecruiterProfile findByUserId(Long userId);

    Optional<RecruiterProfile> findByUser(UserRole user);
}
